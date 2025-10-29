-- 可能回滚的列表，一个记录要回滚的skuid一个记录库存
local id_list= {}
local quantity_list= {}

-- key[sku库存key,sku秒杀库存key,秒杀成功队列key,秒杀成功同步队列key]
-- ages[扣减库存数量,用户id,下单时间戳秒值,TradeDTO信息]

-- 调用放传递的keys 和 values  execute(RedisScript<T> script, List<K> keys, Object... args)
-- local keys = KEYS
-- local values = ARGV;

local function deduction(key,num)
    -- keys[1] = key;
    local value = redis.call("get",key)
    if not value then
        value = 0;
    end
    value = value + num
    -- 变更后库存数量小于
    if(value<0)
    then
        -- 发生超卖
        return false;
    end
    redis.call("set",key,value)

    return true
end

local function rollback()
    for i,k in ipairs (id_list) do
        -- 还原库存
        -- keys[1] = k;
        redis.call("incrby",k,0-quantity_list[i])
    end
    --删除秒杀成功队列相应的记录
    redis.call("HDEL", KEYS[3], ARGV[2] .."_"..ARGV[3])
    --删除同步队列记录相应的记录
    redis.call("HDEL", KEYS[4], ARGV[2] .."_"..ARGV[3])

end

local function execute()
    --通过秒杀成功队列判断用户是否重复提交
    --获取秒杀成功队列
    local flag = redis.call("HGET", KEYS[3], ARGV[2] .."_"..ARGV[3])
    -- hget 获取不到数据返回false而不是nil
    if flag ~= false and tonumber(flag) >= 1
    then
        return false
    end

    for i=1,2,1 do
        -- 扣减库存数量
        -- local num = tonumber(ARGV[1])
        local num = ARGV[1]
        if not num then
            error("Failed to convert argument to number: " .. tostring(ARGV[i]))
        end
        local key=  KEYS[i]
        -- 进行库存扣减，为false 代表扣减失败，要进行回滚
        local result = deduction(key,num)

        -- 回滚
        if (result == false)
        then
            rollback()
            return false
        else
            -- 记录可能要回滚的数据
            table.insert(id_list,key)
            table.insert(quantity_list,num)
            -- 记录成功队列
            redis.call("HSET", KEYS[3], ARGV[2] .."_"..ARGV[3], 1)
            -- 记录同步队列
            redis.call("HSET", KEYS[4], ARGV[2] .."_"..ARGV[3], ARGV[4])
        end

    end
    return true;
end

return execute()