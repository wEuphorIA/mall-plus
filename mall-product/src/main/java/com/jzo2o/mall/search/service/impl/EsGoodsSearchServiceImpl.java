package com.jzo2o.mall.search.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.PageUtil;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jzo2o.common.utils.LambdaUtils;
import com.jzo2o.common.utils.StringUtils;
import com.jzo2o.es.core.ElasticSearchTemplate;
import com.jzo2o.es.utils.SearchResponseUtils;
import com.jzo2o.mall.common.cache.CachePrefix;
import com.jzo2o.mall.common.exception.ServiceException;
import com.jzo2o.mall.common.enums.GoodsAuthEnum;
import com.jzo2o.mall.common.enums.GoodsStatusEnum;
import com.jzo2o.mall.search.model.domain.EsGoodsIndex;
import com.jzo2o.mall.search.model.dto.EsGoodsRelatedInfoDTO;
import com.jzo2o.mall.search.model.dto.EsGoodsSearchDTO;
import com.jzo2o.mall.search.model.dto.ParamOptionsDTO;
import com.jzo2o.mall.search.model.dto.SelectorOptionsDTO;
import com.jzo2o.mall.search.service.EsGoodsSearchService;
import com.jzo2o.mysql.domain.PageVO;
import com.jzo2o.mysql.utils.SqlFilter;
import com.jzo2o.redis.helper.Cache;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction;
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FieldValueFactorFunctionBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ES商品搜索业务层实现
 **/
@Slf4j
@Service
public class EsGoodsSearchServiceImpl implements EsGoodsSearchService {

    private static final String INDEX_NAME = "mall_goods";

    // 最小分词匹配
    private static final String MINIMUM_SHOULD_MATCH = "20%";

    private static final String ATTR_PATH = "attrList";
    private static final String ATTR_VALUE = "attrList.value";
    private static final String ATTR_NAME = "attrList.name";
    private static final String ATTR_SORT = "attrList.sort";
    private static final String ATTR_BRAND_ID = "brandId";
    private static final String ATTR_BRAND_NAME = "brandNameAgg";
    private static final String ATTR_BRAND_URL = "brandUrlAgg";
    private static final String ATTR_NAME_KEY = "nameList";
    private static final String ATTR_VALUE_KEY = "valueList";
    /**
     * ES
     */
//    @Autowired
//    private ElasticsearchOperations restTemplate;

    @Autowired
    private EsGoodsIndexServiceImpl esGoodsIndexService;

    @Autowired
    private ElasticSearchTemplate elasticSearchTemplate;

    /**
     * 缓存
     */
    @Autowired
    private Cache<Object> cache;



//    @Override
//    public Page<EsGoodsIndex> searchGoodsByPage(EsGoodsSearchDTO searchDTO, PageVO pageVo) {
//        Page<EsGoodsIndex> esGoodsIndexPage = this.searchGoods(searchDTO, pageVo);
////        Page<EsGoodsIndex> resultPage = new Page<>();
////        if (esGoodsIndices != null && !esGoodsIndices.isEmpty()) {
////            Pageable pageable = PageRequest.of(pageVo.getPageNumber(), 1);
////            resultPage.setRecords(esGoodsIndices);
////            resultPage.setPages(pageable.getPageNumber());
////            resultPage.setCurrent();
////            resultPage.setSize(pageable.getPageSize());
////            resultPage.setTotal(pageable.get);
////        }
//        return esGoodsIndexPage;
//    }

    @Override
    public Page<EsGoodsIndex> searchGoodsByPage(EsGoodsSearchDTO searchDTO, PageVO pageVo) {
        //todo 完善搜索
        Page<EsGoodsIndex> resultPage = new Page<>();

        return  resultPage;
    }

    private  SearchRequest.Builder createSearchQueryBuilder(EsGoodsSearchDTO searchDTO, PageVO pageVo) {
        // 构造查询条件
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(INDEX_NAME);

        builder.query(query->query.bool(bool->{
            //匹配关键字
            if (ObjectUtil.isNotEmpty(searchDTO.getKeyword())) {
                //商品名称
                String goodsNameField = LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getGoodsName);
                bool.must(must->
                        must.multiMatch(multiMatch->multiMatch.fields(goodsNameField).query(searchDTO.getKeyword())));
            }
            if (ObjectUtil.isNotEmpty(searchDTO.getCategoryId())) {
                //categoryPath
                String categoryPathField = LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getCategoryPath);
//                bool.must(must->must.wildcard(wildcard->wildcard.field(categoryPathField).value("*" + searchDTO.getCategoryId() + "*")));
                bool.must(must->must
                        .term(termQuery->termQuery
                                .field(categoryPathField).value(searchDTO.getCategoryId())));
            }
            if (ObjectUtil.isNotEmpty(searchDTO.getGoodsId())) {
                //goodsId
                String goodsIdField = LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getGoodsId);
                bool.must(must->must.term(termQuery->termQuery.field(goodsIdField).value(searchDTO.getGoodsId())));
            }
            //匹配品牌
            if (CharSequenceUtil.isNotEmpty(searchDTO.getBrandId())) {
                String[] brands = searchDTO.getBrandId().split("@");
                bool.must(must->must.term(termQuery->termQuery.field(StringUtils.toUnderlineCase(ATTR_BRAND_ID)).value(brands[0])));
            }

            //商品参数
            if (CharSequenceUtil.isNotEmpty(searchDTO.getProp())) {
                this.propSearch(bool, searchDTO);
            }


            //价格区间判定
            if (CharSequenceUtil.isNotEmpty(searchDTO.getPrice())) {
                String[] prices = searchDTO.getPrice().split("_");
                if (prices.length > 0) {
                    double min = Convert.toDouble(prices[0], 0.0);
                    double max = Integer.MAX_VALUE;

                    if (prices.length == 2) {
                        max = Convert.toDouble(prices[1], Double.MAX_VALUE);
                    }
                    if (min > max) {
                        throw new ServiceException("价格区间错误");
                    }
                    if (min > Double.MAX_VALUE) {
                        min = Double.MAX_VALUE;
                    }
                    if (max > Double.MAX_VALUE) {
                        max = Double.MAX_VALUE;
                    }
                    double finalMin = min;
                    double finalMax = max;
                    bool.must(must->must
                            .range(rnageQuery->rnageQuery
                                    .field(LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getPrice))
                                    .from(String.valueOf(finalMin))
                                    .to(String.valueOf(finalMax))));
                }
            }

            //未上架的商品不显示
            bool.filter(filter->filter
                    .term(term->term
                            .field(LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getMarketEnable))
                            .value(GoodsStatusEnum.UPPER.name())));
            //待审核和审核不通过的商品不显示
            bool.filter(filter->filter
                    .term(term->term
                            .field(LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getAuthFlag))
                            .value(GoodsAuthEnum.PASS.name())));

            return bool;
        }));

        if(pageVo!=null){
            //pageNumber=0&pageSize=20
            int start = PageUtil.getStart(pageVo.getPageNumber()-1, pageVo.getPageSize());
            // 4.查询数量限制
            builder = builder.from(start).size( pageVo.getPageSize());
        }

        //排序
        if (pageVo != null && CharSequenceUtil.isNotEmpty(pageVo.getOrder()) && CharSequenceUtil.isNotEmpty(pageVo.getSort())) {
            SortOrder sortOrder = SortOrder.Desc;
            if(pageVo.getOrder().equals("asc")){
                sortOrder = SortOrder.Asc;
            }
            SortOrder finalSortOrder = sortOrder;
            builder.sort(sortOptions->sortOptions.field(fieldSort->fieldSort.field(StringUtils.toUnderlineCase(pageVo.getSort())).order(finalSortOrder)));
        }
        return builder;
    }
    private  SearchRequest.Builder createSearchRelatedBuilder(EsGoodsSearchDTO searchDTO, PageVO pageVo) {
        // 构造查询条件
        SearchRequest.Builder builder = new SearchRequest.Builder();
        builder.index(INDEX_NAME);

        builder.query(query->query.bool(bool->{
            //匹配关键字
            if (ObjectUtil.isNotEmpty(searchDTO.getKeyword())) {
                //商品名称
                String goodsNameField = LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getGoodsName);
                bool.must(must->
                        must.multiMatch(multiMatch->multiMatch.fields(goodsNameField).query(searchDTO.getKeyword())));
            }
            if (ObjectUtil.isNotEmpty(searchDTO.getCategoryId())) {
                //categoryPath
                String categoryPathField = LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getCategoryPath);
//                bool.must(must->must.wildcard(wildcard->wildcard.field(categoryPathField).value("*" + searchDTO.getCategoryId() + "*")));
                bool.must(must->must.term(termQuery->termQuery.field(categoryPathField).value(searchDTO.getCategoryId())));
            }
//            if (ObjectUtil.isNotEmpty(searchDTO.getGoodsId())) {
//                //goodsId
//                String goodsIdField = LambdaUtils.getUnderLineFieldName(EsGoodsIndex::getGoodsId);
//                bool.must(must->must.term(termQuery->termQuery.field(goodsIdField).value(searchDTO.getGoodsId())));
//            }

            return bool;
        }));


        return builder;
    }

    /**
     * 商品参数查询处理
     *
     * @param builder 查询条件构造器
     * @param searchDTO     查询参数
     */
    private void propSearch(BoolQuery.Builder builder, EsGoodsSearchDTO searchDTO) {
        //示例：5G网络_支持5G@充电接口_Type-C@5G网络_支持5G@内存_128G@内存_256G
        String[] props = searchDTO.getProp().split("@");
        Set<String> nameList = new HashSet<>();
        Set<String> valueList = new HashSet<>();
        Map<String, Set<String>> valueMap = new HashMap<>(16);
        for (String prop : props) {
            String[] propValues = prop.split("_");
            String name = propValues[0];
            String value = propValues[1];
            nameList.add(name);
            valueList.add(value);
            //将同一规格名下的规格值分组
            if (!valueMap.containsKey(name)) {
                Set<String> values = new HashSet<>();
                values.add(value);
                valueMap.put(name, values);
            } else {
                valueMap.get(name).add(value);
            }
        }
        for (Map.Entry<String, Set<String>> entry : valueMap.entrySet()) {
            builder.must(mustBuilder->mustBuilder
                    .nested(nested->nested
                            .path("attr_list").query(query->query
                                    .bool(boolQuery->{
                                            boolQuery.must(must->must.match(match->match.field("attr_list.name").query(entry.getKey())));
                                            for (String s : entry.getValue()) {
                                                boolQuery.must(must->must.match(match->match.field("attr_list.value").query(s)));
                                            }
                                        return boolQuery;
                                    }))));
        }
        //boolQuery.must(must->must.terms(terms->terms.field("attr_list.name").terms(term->term.value(nameList))))
        //遍历所有的规格

//        for (Map.Entry<String, Set<String>> entry : valueMap.entrySet()) {
//            BoolQuery.Builder attrBuilder = builder.must(filterBuilder -> filterBuilder.nested(nested -> nested.path("attr_list").query(query -> query.match(match -> match.field("attr_list.name").query(entry.getKey())))));
////            filterBuilder.must(QueryBuilders.nestedQuery(ATTR_PATH, QueryBuilders.matchQuery(ATTR_NAME, entry.getKey()), ScoreMode.None));
////            BoolQueryBuilder shouldBuilder = QueryBuilders.boolQuery();
//            for (String s : entry.getValue()) {
//                attrBuilder.must(
//                        filterBuilder->filterBuilder.nested(nested->nested.path("attr_list").query(query->query.match(match->match.field("attr_list.value").query(s))))
//                );
////                shouldBuilder.should(QueryBuilders.nestedQuery(ATTR_PATH, QueryBuilders.matchQuery(ATTR_VALUE, s), ScoreMode.None));
//            }
////            builder.must(shouldBuilder);
//        }
//        searchDTO.getNotShowCol().put(ATTR_NAME_KEY, nameList);
//        searchDTO.getNotShowCol().put(ATTR_VALUE_KEY, valueList);
    }

    @Override
    public EsGoodsRelatedInfoDTO getSelector(EsGoodsSearchDTO goodsSearch, PageVO pageVo) {
        //todo 待完善
        Map<String, Aggregate> aggregationMap = new HashMap<>();
        return convertToEsGoodsRelatedInfo(aggregationMap, goodsSearch);
    }


//    @Override
//    public List<EsGoodsIndex> getEsGoodsBySkuIds(List<String> skuIds, PageVO pageVo) {
//        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
//        if (pageVo != null) {
//            int pageNumber = pageVo.getPageNumber() - 1;
//            if (pageNumber < 0) {
//                pageNumber = 0;
//            }
//            Pageable pageable = PageRequest.of(pageNumber, pageVo.getPageSize());
//            //分页
//            searchQueryBuilder.withPageable(pageable);
//        }
//        NativeSearchQuery build = searchQueryBuilder.build();
//        build.setIds(skuIds);
//        return restTemplate.multiGet(build, EsGoodsIndex.class, restTemplate.getIndexCoordinatesFor(EsGoodsIndex.class));
//    }

    public List<EsGoodsIndex> getEsGoodsByGoodsId(String goodsId){
        EsGoodsSearchDTO esGoodsSearchDTO = new EsGoodsSearchDTO();
        esGoodsSearchDTO.setGoodsId(goodsId);
        SearchRequest.Builder builder = createSearchQueryBuilder(esGoodsSearchDTO, null);
        // 检索数据
        SearchResponse<EsGoodsIndex> searchResponse = elasticSearchTemplate.opsForDoc().search(builder.build(), EsGoodsIndex.class);
        Page<EsGoodsIndex> resultPage = new Page<>();
        //如果搜索成功返回结果集
        if (SearchResponseUtils.isSuccess(searchResponse)) {
            //总记录数
            long total = searchResponse.hits().total().value();

            List<EsGoodsIndex> collect = searchResponse.hits().hits()
                    .stream().map(hit -> {
                        EsGoodsIndex goodsIndex = hit.source();
                        return goodsIndex;
                    })
                    .collect(Collectors.toList());

            return collect;
        }
        return  new ArrayList<>();
    }
    /**
     * 根据id获取商品索引
     *
     * @param id 商品skuId
     * @return 商品索引
     */
    @Override
    public EsGoodsIndex getEsGoodsById(String id) {

        EsGoodsIndex goodsIndex = esGoodsIndexService.findById(id);

        return goodsIndex;
    }

    /**
     * 转换搜索结果为聚合商品展示信息
     *
     * @param aggregationMap 搜索结果
     * @return 聚合商品展示信息
     */
    private EsGoodsRelatedInfoDTO convertToEsGoodsRelatedInfo(Map<String, Aggregate> aggregationMap, EsGoodsSearchDTO goodsSearch) {
        EsGoodsRelatedInfoDTO esGoodsRelatedInfo = new EsGoodsRelatedInfoDTO();
        //分类
        List<StringTermsBucket> categoryAgg = aggregationMap.get("categoryAgg").sterms().buckets().array();
        List<SelectorOptionsDTO> selectorOptionsDTOS = convertCategoryOptions(categoryAgg);
        esGoodsRelatedInfo.setCategories(selectorOptionsDTOS);

        //品牌
        List<StringTermsBucket> brandIdAgg = aggregationMap.get("brandIdAgg").sterms().buckets().array();
        List<SelectorOptionsDTO> brandOptions = convertBrandOptions(brandIdAgg);
        esGoodsRelatedInfo.setBrands(brandOptions);

        //商品参数
        List<StringTermsBucket> attrNameAgg = aggregationMap.get("attrAgg").nested().aggregations().get("nameAgg").sterms().buckets().array();
        List<ParamOptionsDTO> paramOptions = convertParamOptions(attrNameAgg);
        esGoodsRelatedInfo.setParamOptions(paramOptions);

        return esGoodsRelatedInfo;
    }

   /**
     * 将分类聚合结果转换分类选择项
     *
     * @param categoryAgg 分类聚合结果
     * @return 分类选择项集合
     */
    private List<SelectorOptionsDTO> convertCategoryOptions(List<StringTermsBucket> categoryAgg) {
        List<SelectorOptionsDTO> categoryOptions = new ArrayList<>();

            for (StringTermsBucket categoryAggBucket: categoryAgg) {
                log.info("There are " + categoryAggBucket.docCount() +
                        " bikes under " + categoryAggBucket.key().stringValue());
                String categoryAggBucketKey = categoryAggBucket.key().stringValue();
                String[] cucketKeyArray = categoryAggBucketKey.split("\\|");
                String categoryNamePath = cucketKeyArray[0];
                String categoryPath = cucketKeyArray[1];
                String[] split = ArrayUtil.distinct(categoryPath.split(","));
                String[] nameSplit = categoryNamePath.split(",");
                if (split.length == nameSplit.length) {
                    for (int i = 0; i < split.length; i++) {
                        SelectorOptionsDTO so = new SelectorOptionsDTO();
                        so.setName(nameSplit[i]);
                        so.setValue(split[i]);
                        if (!categoryOptions.contains(so)) {
                            categoryOptions.add(so);
                        }
                    }
                }

            }

//        for (StringTermsBucket bucket: categoryAgg) {
//            log.info("There are " + bucket.docCount() +
//                    " bikes under " + bucket.key().stringValue());
//            String categoryPath =  bucket.key().stringValue();
//
//            Map<String, Aggregate> aggregations = bucket.aggregations();
//            List<StringTermsBucket> categoryNameAgg = aggregations.get("categoryNameAgg").sterms().buckets().array();
//            for (StringTermsBucket categoryNameBucket: categoryNameAgg) {
//                log.info("There are " + categoryNameBucket.docCount() +
//                        " bikes under " + categoryNameBucket.key().stringValue());
//                String categoryNamePath = categoryNameBucket.key().stringValue();
//                String[] split = ArrayUtil.distinct(categoryPath.split(","));
//                String[] nameSplit = categoryNamePath.split(",");
//                if (split.length == nameSplit.length) {
//                    for (int i = 0; i < split.length; i++) {
//                        SelectorOptionsDTO so = new SelectorOptionsDTO();
//                        so.setName(nameSplit[i]);
//                        so.setValue(split[i]);
//                        if (!categoryOptions.contains(so)) {
//                            categoryOptions.add(so);
//                        }
//                    }
//                }
//
//            }
//
//        }

        return categoryOptions;
    }
   /**
     * 将商品参数聚合结果转换商品参数选项
     *
     * @param attrNameAgg 商品参数聚合结果
     * @return 商品参数选择项集合
     */
    private List<ParamOptionsDTO> convertParamOptions(List<StringTermsBucket> attrNameAgg) {

        List<ParamOptionsDTO> paramOptionsDTOS = new ArrayList<>();

        for (StringTermsBucket bucket: attrNameAgg) {
            log.info("There are " + bucket.docCount() +
                    " bikes under " + bucket.key().stringValue());
            String attrName =  bucket.key().stringValue();
            ParamOptionsDTO so = new ParamOptionsDTO();
            //参数名称
            so.setKey(attrName);
            //参数值
            List<String> values = new ArrayList<String>();
            Map<String, Aggregate> aggregations = bucket.aggregations();
            List<StringTermsBucket> valueAgg = aggregations.get("valueAgg").sterms().buckets().array();
            for (StringTermsBucket valueBucket: valueAgg) {
                log.info("There are " + valueBucket.docCount() +
                        " bikes under " + valueBucket.key().stringValue());
                String value = valueBucket.key().stringValue();
                values.add(value);

            }
            so.setValues(values);
            paramOptionsDTOS.add(so);

        }
        return paramOptionsDTOS;
    }
   /**
     * 将品牌聚合结果转换品牌选择项
     *
     * @param brandBuckets 品牌聚合结果
     * @return 品牌选择项集合
     */
    private List<SelectorOptionsDTO> convertBrandOptions(List<StringTermsBucket> brandBuckets) {
        List<SelectorOptionsDTO> brandOptions = new ArrayList<>();

        for (StringTermsBucket bucket: brandBuckets) {
            log.info("There are " + bucket.docCount() +
                    " bikes under " + bucket.key().stringValue());
            String brandId =  bucket.key().stringValue();

            Map<String, Aggregate> aggregations = bucket.aggregations();
            List<StringTermsBucket> brandNameAgg = aggregations.get("brandNameAgg").sterms().buckets().array();

            for (StringTermsBucket brandNameBucket: brandNameAgg) {
                //品牌名称
                String brandName = brandNameBucket.key().stringValue();
                Map<String, Aggregate> brandUrlAggregate = brandNameBucket.aggregations();
                List<StringTermsBucket> brandUrlAgg = brandUrlAggregate.get("brandUrlAgg").sterms().buckets().array();

                for (StringTermsBucket brandUrlBucket: brandUrlAgg) {
                    //品牌图片url
                    String brandUrl = brandUrlBucket.key().stringValue();
                    SelectorOptionsDTO so = new SelectorOptionsDTO();
                    so.setName(brandName);
                    so.setValue(brandId);
                    so.setUrl(brandUrl);
                    if (!brandOptions.contains(so)) {
                        brandOptions.add(so);
                    }
                }



            }

        }

        return brandOptions;
    }


    /**
     * 获取品牌聚合结果内的参数
     *
     * @param brandAgg 品牌聚合结果
     * @return 品牌聚合结果内的参数
     */
    private String getAggregationsBrandOptions(ParsedStringTerms brandAgg) {
        List<? extends Terms.Bucket> brandAggBuckets = brandAgg.getBuckets();
        if (brandAggBuckets != null && !brandAggBuckets.isEmpty()) {
            return brandAggBuckets.get(0).getKey().toString();
        }
        return "";
    }


//    /**
//     * 将分类聚合结果转换分类选择项
//     *
//     * @param categoryBuckets 分类聚合结果
//     * @return 分类选择项集合
//     */
//    private List<SelectorOptions> convertCategoryOptions(List<? extends Terms.Bucket> categoryBuckets) {
//        List<SelectorOptions> categoryOptions = new ArrayList<>();
//        for (Terms.Bucket categoryBucket : categoryBuckets) {
//            String categoryPath = categoryBucket.getKey().toString();
//            ParsedStringTerms categoryNameAgg = categoryBucket.getAggregations().get("categoryNameAgg");
//            List<? extends Terms.Bucket> categoryNameBuckets = categoryNameAgg.getBuckets();
//
//
//            if (!categoryNameBuckets.isEmpty()) {
//                String categoryNamePath = categoryNameBuckets.get(0).getKey().toString();
//                String[] split = ArrayUtil.distinct(categoryPath.split(","));
//                String[] nameSplit = categoryNamePath.split(",");
//                if (split.length == nameSplit.length) {
//                    for (int i = 0; i < split.length; i++) {
//                        SelectorOptions so = new SelectorOptions();
//                        so.setName(nameSplit[i]);
//                        so.setValue(split[i]);
//                        if (!categoryOptions.contains(so)) {
//                            categoryOptions.add(so);
//                        }
//                    }
//                }
//            }
//
//        }
//        return categoryOptions;
//    }

//    /**
//     * 构建商品参数信息
//     *
//     * @param attrTerms 商品参数搜索结果
//     * @param nameList  查询的规格名
//     * @return 商品参数信息
//     */
//    private List<ParamOptions> buildGoodsParam(ParsedNested attrTerms, List<String> nameList) {
//        if (attrTerms != null) {
//            Aggregations attrAggregations = attrTerms.getAggregations();
//            Map<String, Aggregation> attrMap = attrAggregations.getAsMap();
//            ParsedStringTerms nameAgg = (ParsedStringTerms) attrMap.get("nameAgg");
//
//            if (nameAgg != null) {
//                return this.buildGoodsParamOptions(nameAgg, nameList);
//            }
//
//        }
//        return new ArrayList<>();
//    }

//    /**
//     * 构造商品参数属性
//     *
//     * @param nameAgg  商品参数聚合内容
//     * @param nameList 查询的规格名
//     * @return 商品参数属性集合
//     */
//    private List<ParamOptions> buildGoodsParamOptions(ParsedStringTerms nameAgg, List<String> nameList) {
//        List<ParamOptions> paramOptions = new ArrayList<>();
//        List<? extends Terms.Bucket> nameBuckets = nameAgg.getBuckets();
//
//        for (Terms.Bucket bucket : nameBuckets) {
//            String name = bucket.getKey().toString();
//            ParamOptions paramOptions1 = new ParamOptions();
//            ParsedStringTerms valueAgg = bucket.getAggregations().get("valueAgg");
//            List<? extends Terms.Bucket> valueBuckets = valueAgg.getBuckets();
//            List<String> valueSelectorList = new ArrayList<>();
//
//            for (Terms.Bucket valueBucket : valueBuckets) {
//                String value = valueBucket.getKey().toString();
//
//                if (CharSequenceUtil.isNotEmpty(value)) {
//                    valueSelectorList.add(value);
//                }
//
//            }
//            if (nameList == null || !nameList.contains(name)) {
//                paramOptions1.setKey(name);
//                paramOptions1.setValues(valueSelectorList);
//                paramOptions.add(paramOptions1);
//            }
//        }
//        return paramOptions;
//    }
//
//    /**
//     * 创建es搜索builder
//     *
//     * @param searchDTO 搜索条件
//     * @param pageVo    分页参数
//     * @return es搜索builder
//     */
//    private NativeSearchQueryBuilder createSearchQueryBuilder(EsGoodsSearchDTO searchDTO, PageVO pageVo) {
//        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
//        if (pageVo != null) {
//            int pageNumber = pageVo.getPageNumber() - 1;
//            if (pageNumber < 0) {
//                pageNumber = 0;
//            }
//            Pageable pageable = PageRequest.of(pageNumber, pageVo.getPageSize());
//            //分页
//            nativeSearchQueryBuilder.withPageable(pageable);
//        }
//        //查询参数非空判定
//        if (searchDTO != null) {
//            //过滤条件
//            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
//
//            //对查询条件进行处理
//            this.commonSearch(boolQueryBuilder, searchDTO);
//
//            //智能推荐
//            this.recommended(boolQueryBuilder, searchDTO);
//
//            //未上架的商品不显示
//            boolQueryBuilder.must(QueryBuilders.matchQuery("marketEnable", GoodsStatusEnum.UPPER.name()));
//            //待审核和审核不通过的商品不显示
//            boolQueryBuilder.must(QueryBuilders.matchQuery("authFlag", GoodsAuthEnum.PASS.name()));
//
//
//            //关键字检索
//            if (CharSequenceUtil.isEmpty(searchDTO.getKeyword())) {
//                List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = this.buildFunctionSearch();
//                FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
//                filterFunctionBuilders.toArray(builders);
//                FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(QueryBuilders.matchAllQuery(), builders)
//                        .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
//                        .setMinScore(2);
//                //聚合搜索则将结果放入过滤条件
//                boolQueryBuilder.must(functionScoreQueryBuilder);
//            } else {
//                this.keywordSearch(boolQueryBuilder, searchDTO.getKeyword());
//            }
//
//            //如果是聚合查询
//
//            nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
//
//
//            if (pageVo != null && CharSequenceUtil.isNotEmpty(pageVo.getOrder()) && CharSequenceUtil.isNotEmpty(pageVo.getSort())) {
//                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(pageVo.getSort()).order(SortOrder.valueOf(pageVo.getOrder().toUpperCase())));
//            } else {
//                nativeSearchQueryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
//            }
//
//        }
//        return nativeSearchQueryBuilder;
//    }
//
//    /**
//     * 商品推荐
//     *
//     * @param filterBuilder 过滤条件
//     * @param searchDTO     搜索条件
//     */
//    private void recommended(BoolQueryBuilder filterBuilder, EsGoodsSearchDTO searchDTO) {
//
//        String currentGoodsId = searchDTO.getCurrentGoodsId();
//        if (CharSequenceUtil.isEmpty(currentGoodsId)) {
//            return;
//        }
//
//        //排除当前商品
//        filterBuilder.mustNot(QueryBuilders.matchQuery("id", currentGoodsId));
//
//        //查询当前浏览商品的索引信息
//        EsGoodsIndex esGoodsIndex = restTemplate.get(currentGoodsId, EsGoodsIndex.class);
//        if (esGoodsIndex == null) {
//            return;
//        }
//        //推荐与当前浏览商品相同一个二级分类下的商品
//        String categoryPath = esGoodsIndex.getCategoryPath();
//        if (CharSequenceUtil.isNotEmpty(categoryPath)) {
//            //匹配二级分类
//            String substring = categoryPath.substring(0, categoryPath.lastIndexOf(","));
//            filterBuilder.must(QueryBuilders.wildcardQuery("categoryPath", substring + "*"));
//        }
//
//    }

    /**
     * 查询属性处理
     *
     * @param boolQueryBuilder 布尔查询构造器
     * @param searchDTO     查询参数
     */
    private void commonSearch(BoolQueryBuilder boolQueryBuilder, EsGoodsSearchDTO searchDTO) {
        //品牌判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getBrandId())) {
            String[] brands = searchDTO.getBrandId().split("@");
            boolQueryBuilder.must(QueryBuilders.termsQuery(ATTR_BRAND_ID, brands));
        }
        if (searchDTO.getRecommend() != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("recommend", searchDTO.getRecommend()));
        }
        // 商品类型判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getGoodsType())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("goodsType", searchDTO.getGoodsType()));
        }
        if (CharSequenceUtil.isNotEmpty(searchDTO.getNeGoodsType())) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("goodsType", searchDTO.getNeGoodsType()));
        }
        // 销售类型判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getSalesModel())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("salesModel", searchDTO.getSalesModel()));
        }
        if (CharSequenceUtil.isNotEmpty(searchDTO.getNeSalesModel())) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("salesModel", searchDTO.getNeSalesModel()));
        }
        //规格项判定
        if (searchDTO.getNameIds() != null && !searchDTO.getNameIds().isEmpty()) {
            boolQueryBuilder.must(QueryBuilders.nestedQuery(ATTR_PATH, QueryBuilders.termsQuery("attrList.nameId", searchDTO.getNameIds()), ScoreMode.None));
        }
        //分类判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getCategoryId())) {
//            boolQueryBuilder.must(QueryBuilders.wildcardQuery("categoryPath", "*" + searchDTO.getCategoryId() + "*"));
            boolQueryBuilder.must(QueryBuilders.termQuery("categoryPath", searchDTO.getCategoryId()));
        }
        //店铺分类判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getStoreCatId())) {
//            boolQueryBuilder.must(QueryBuilders.wildcardQuery("storeCategoryPath", "*" + searchDTO.getStoreCatId() + "*"));
            boolQueryBuilder.must(QueryBuilders.termQuery("storeCategoryPath", searchDTO.getStoreCatId()));
        }
        //店铺判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getStoreId())) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("storeId", searchDTO.getStoreId()));
        }
        //属性判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getProp())) {
            this.propSearch(boolQueryBuilder, searchDTO);
        }
        // 促销活动判定
//        if (CharSequenceUtil.isNotEmpty(searchDTO.getPromotionsId()) && CharSequenceUtil.isNotEmpty(searchDTO.getPromotionType())) {
//            boolQueryBuilder.must(QueryBuilders.wildcardQuery("promotionMapJson", "*" + searchDTO.getPromotionType() + "-" + searchDTO.getPromotionsId() + "*"));
//        }
        //价格区间判定
        if (CharSequenceUtil.isNotEmpty(searchDTO.getPrice())) {
            String[] prices = searchDTO.getPrice().split("_");
            if (prices.length == 0) {
                return;
            }
            double min = Convert.toDouble(prices[0], 0.0);
            double max = Integer.MAX_VALUE;

            if (prices.length == 2) {
                max = Convert.toDouble(prices[1], Double.MAX_VALUE);
            }
            if (min > max) {
                throw new ServiceException("价格区间错误");
            }
            if (min > Double.MAX_VALUE) {
                min = Double.MAX_VALUE;
            }
            if (max > Double.MAX_VALUE) {
                max = Double.MAX_VALUE;
            }
            boolQueryBuilder.must(QueryBuilders.rangeQuery("price").from(min).to(max).includeLower(true).includeUpper(true));
        }
    }

    /**
     * 商品参数查询处理
     *
     * @param filterBuilder 过滤构造器
     * @param searchDTO     查询参数
     */
    private void propSearch(BoolQueryBuilder filterBuilder, EsGoodsSearchDTO searchDTO) {
        String[] props = searchDTO.getProp().split("@");
        List<String> nameList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();
        Map<String, List<String>> valueMap = new HashMap<>(16);
        for (String prop : props) {
            String[] propValues = prop.split("_");
            String name = propValues[0];
            String value = propValues[1];
            if (!nameList.contains(name)) {
                nameList.add(name);
            }
            if (!valueList.contains(value)) {
                valueList.add(value);
            }
            //将同一规格名下的规格值分组
            if (!valueMap.containsKey(name)) {
                List<String> values = new ArrayList<>();
                values.add(value);
                valueMap.put(name, values);
            } else {
                valueMap.get(name).add(value);
            }
        }
        //遍历所有的规格
        for (Map.Entry<String, List<String>> entry : valueMap.entrySet()) {
            filterBuilder.must(QueryBuilders.nestedQuery(ATTR_PATH, QueryBuilders.matchQuery(ATTR_NAME, entry.getKey()), ScoreMode.None));
            BoolQueryBuilder shouldBuilder = QueryBuilders.boolQuery();
            for (String s : entry.getValue()) {
                shouldBuilder.should(QueryBuilders.nestedQuery(ATTR_PATH, QueryBuilders.matchQuery(ATTR_VALUE, s), ScoreMode.None));
            }
            filterBuilder.must(shouldBuilder);
        }
        searchDTO.getNotShowCol().put(ATTR_NAME_KEY, nameList);
        searchDTO.getNotShowCol().put(ATTR_VALUE_KEY, valueList);
    }

    /**
     * 关键字查询处理
     *
     * @param filterBuilder 过滤构造器
     * @param keyword       关键字
     */
    private void keywordSearch(BoolQueryBuilder filterBuilder, String keyword) {

        List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = this.buildFunctionSearch();

        //分词匹配
        // operator 为 AND 时 需全部分词匹配。为 OR 时 需配置 minimumShouldMatch（最小分词匹配数）不设置默认为1
        MatchQueryBuilder goodsNameMatchQuery = QueryBuilders.matchQuery("goodsName", keyword).operator(Operator.OR).minimumShouldMatch(MINIMUM_SHOULD_MATCH);

        FunctionScoreQueryBuilder.FilterFunctionBuilder[] builders = new FunctionScoreQueryBuilder.FilterFunctionBuilder[filterFunctionBuilders.size()];
        filterFunctionBuilders.toArray(builders);
        FunctionScoreQueryBuilder functionScoreQueryBuilder = QueryBuilders.functionScoreQuery(goodsNameMatchQuery, builders)
                .scoreMode(FunctionScoreQuery.ScoreMode.SUM)
                .setMinScore(2);
        //聚合搜索则将结果放入过滤条件
        filterBuilder.must(functionScoreQueryBuilder);
        filterBuilder.should(QueryBuilders.boolQuery().should(QueryBuilders.matchPhraseQuery("goodsName", keyword).boost(10)));
    }

    /**
     * 构造关键字查询
     *
     * @return 构造查询的集合
     */
    private List<FunctionScoreQueryBuilder.FilterFunctionBuilder> buildFunctionSearch() {
        List<FunctionScoreQueryBuilder.FilterFunctionBuilder> filterFunctionBuilders = new ArrayList<>();

        // 修改分数算法为无，数字最大分数越高
        FieldValueFactorFunctionBuilder skuNoScore = ScoreFunctionBuilders.fieldValueFactorFunction("skuSource").modifier(FieldValueFactorFunction.Modifier.LOG1P).setWeight(3);
        FunctionScoreQueryBuilder.FilterFunctionBuilder skuNoBuilder = new FunctionScoreQueryBuilder.FilterFunctionBuilder(skuNoScore);
        filterFunctionBuilders.add(skuNoBuilder);

        return filterFunctionBuilders;
    }

}
