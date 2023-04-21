package com.yuhaojituan.comment.service.impl;

import com.yuhaojituan.comment.service.CommentHotService;
import com.yuhaojituan.comment.service.CommentService;
import com.yuhaojituan.feigns.UserFeign;
import com.yuhaojituan.model.comment.dots.CommentDTO;
import com.yuhaojituan.model.comment.dots.CommentLikeDTO;
import com.yuhaojituan.model.comment.dots.CommentSaveDTO;
import com.yuhaojituan.model.comment.pojos.ApComment;
import com.yuhaojituan.model.comment.pojos.ApCommentLike;
import com.yuhaojituan.model.comment.vos.ApCommentVo;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.threadlocal.AppThreadLocalUtils;
import com.yuhaojituan.model.user.pojos.ApUser;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private UserFeign userFeign;
    @Autowired
    MongoTemplate mongoTemplate;

    //TODO 有时间 比较一下代码 like save方法
    //https://gitee.com/xiaoT_CJ/heima-leadnews141/blob/master/heima-leadnews-services/comment-service/src/main/java/com/heima/comment/service/impl/CommentServiceImpl.java
    @Override
    public ResponseResult saveComment(CommentSaveDTO dto) {
//        1. 校验参数
//       校验是否登录/校验文章id/校验内容不为空  校验内容长度不能大于140个字符 (validated校验)
        //获取filter存的user对象 filter只是从hear里获取了id（gateway放入的id）
        ApUser user = AppThreadLocalUtils.getUser();
        if (user.getId() == 0) {
            return ResponseResult.okResult("设备登录无法评论");
        }
        if (dto.getArticleId() == null) {
            return ResponseResult.errorResult(1, "文章id为空");
        }
        if (dto.getContent() == null || dto.getContent().length() > 140) {
            return ResponseResult.errorResult(2, "评论有问题");
        }
//        2. 阿里云校验评论内容是否违规
//        3. 远程查询当前登陆用户信息
        ResponseResult<ApUser> userById = userFeign.findUserById(user.getId());
        ApUser author = userById.getData();
//        4. 创建评论信息，并保存到mongo
        ApComment comment = new ApComment();
        comment.setContent(dto.getContent());
        comment.setCreatedTime(new Date());
        comment.setArticleId(dto.getArticleId());
        comment.setAuthorId(user.getId());
        comment.setLikes(0);
        comment.setReply(0);
        comment.setAuthorName(author.getName());
        comment.setFlag((short) 0);
        mongoTemplate.save(comment);
        return ResponseResult.okResult();
    }

    @Autowired
    CommentHotService commentHotService;

    @Override
    public ResponseResult like(CommentLikeDTO dto) {

        HashMap<Object, Object> map = new HashMap<>();
        ApUser user = AppThreadLocalUtils.getUser();

//        1. 参数校验
//        评论id不能为空  operation必须为 0  或  1
//        2. 根据评论id查询评论数据， 为null返回错误信息
//        3. 如果是点赞操作 判断是否已经点赞
//        点过赞提示 请勿重复点赞
//        未点过赞   保存点赞信息到mongo
//        并修改评论信息的点赞数量( + 1)
        if (dto.getOperation() == 0) {
            ApCommentLike apCommentLike = new ApCommentLike();
            apCommentLike.setCommentId(dto.getCommentId());
            apCommentLike.setAuthorId(user.getId());
            mongoTemplate.save(apCommentLike);

            Query query = Query.query(Criteria.where("_id").is(dto.getCommentId()));
            ApComment one = mongoTemplate.findOne(query, ApComment.class);
            Integer likes = one.getLikes();
            likes++;
            Update update = new Update();
            update.set("likes", likes);
            mongoTemplate.updateFirst(query, update, ApComment.class);
            map.put("likes", likes);
            //计算热点评论评论
            if (likes >= 10 && one.getFlag().shortValue() == 0) {
                commentHotService.hotCommentExecutor(one);
            }
        }
//        4. 如果是取消点赞操作
//                删除点赞信息
//      并修改评论信息的点赞数量( - 1) , 要判断下别减成负数
        if (dto.getOperation() == 1) {
            //delete
            Query likeQuery = Query.query(Criteria.where("commentId").is(dto.getCommentId()).and("authorId").is(user.getId()));
            mongoTemplate.remove(likeQuery, ApCommentLike.class);
            //修改评论
            Query query = Query.query(Criteria.where("_id").is(dto.getCommentId()));
            ApComment one = mongoTemplate.findOne(query, ApComment.class);
            Integer likes = one.getLikes();
            likes--;
            if (likes < 0) {
                likes = 0;
            }
            Update update = new Update();
            update.set("likes", likes);
            mongoTemplate.updateFirst(query, update, ApComment.class);
            map.put("likes", likes);

        }
//        5. 返回结果时，需要返回点赞点赞数量  返回的key要求必须是:  likes
        return ResponseResult.okResult(map);
    }

    @Override
    public ResponseResult findByArticleId(CommentDTO dto) {

        //1 参数检查
        if (dto == null || dto.getArticleId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }
        Integer size = dto.getSize();
        if (size == null || size <= 0) {
            size = 10;
        }
        // 判断是否
        //2 查询Mongo文章所有评论列表
        List<ApComment> apCommentList;
        // 文章评论的首页
        if (dto.getIndex().intValue() == 1) {
            //查询热点评论  5
            Query query = Query.query(Criteria.where("articleId").is(dto.getArticleId()).and("flag").is(1)).with(Sort.by(Sort.Direction.DESC, "likes"));
            apCommentList = mongoTemplate.find(query, ApComment.class);
            // 热点集合不为空
            if (CollectionUtils.isNotEmpty(apCommentList)) {
                size = size - apCommentList.size();
                List<ApComment> normalComments = mongoTemplate.find(getQuery(dto, size), ApComment.class);
                apCommentList.addAll(normalComments);
            } else {
                apCommentList = mongoTemplate.find(
                        getQuery(dto, size),
                        ApComment.class
                );
            }
        } else {
            apCommentList = mongoTemplate.find(
                    getQuery(dto, size),
                    ApComment.class
            );
        }
        //3 封装查询结果
        //3.1 用户未登录 直接返回评论列表
        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            return ResponseResult.okResult(apCommentList);
        }
        //3.2 用户登录，需要加载当前用户对评论点赞的列表
        // 获取文章对应的所有评论ID列表
        List<String> idList = apCommentList.stream()
                .map(ApComment::getId)
                .collect(Collectors.toList());
        // 查询 点赞批量列表 按照评论id 筛选
        List<ApCommentLike> apCommentLikes = mongoTemplate.find(
                Query.query(Criteria.where("commentId").in(idList)
                        .and("authorId").is(user.getId()))
                , ApCommentLike.class);
        // 遍历当前用户点赞列表 和当前评论列表
        if (CollectionUtils.isNotEmpty(apCommentList)
                && CollectionUtils.isNotEmpty(apCommentLikes)) {
            // 获取点过赞的评论id
            List<String> commentIds = apCommentLikes.stream()
                    .map(ApCommentLike::getCommentId).collect(Collectors.toList());
            // 遍历评论列表，将comment 转为 commentVO
            //TODO 这里没有collect  但是功能还是正常
            return ResponseResult.okResult(apCommentList.stream()
                    .map(comment -> parseCommentVO(comment, commentIds)));
        }
        return ResponseResult.okResult(apCommentList);


//v1
//        ApUser user = AppThreadLocalUtils.getUser();
//
////        1. 参数校验 校验文章id    校验size
////        2. 根据条件查询评论列表   (文章id  , 创建时间 小于最小时间 , 截取size条记录 , 创建时间降序)
//        Query query = Query.query(Criteria.where("articleId").is(dto.getArticleId()));
////        Pageable pageable = PageRequest.of(1, dto.getSize());
////        query.with(pageable);
////        Sort sort = Sort.by(Sort.Direction.DESC, "createdTime");
////        query.with(sort);
//        List<ApComment> list = mongoTemplate.find(query, ApComment.class);
//
////        3. 判断当前用户是否登录
////        4. 如果未登录直接返回评论列表
//        if (user.getId() == 0) {
//            return ResponseResult.okResult(list);
//        }
//
//        //        5. 如果登录了 需要检查在当前评论列表中 哪些评论登陆人点赞过
////                (将所有ApComment 转成 ApCommentVO , 点过赞的operation设置为0)
////        推荐实现:
////        5.1 根据当前列表中评论id  和  登录人id 查询评论点赞表，得到点赞数据
////             commentId  in    评论id列表   authorId = 登录人id
//
//        ArrayList<ApCommentVo> vos = new ArrayList<>();
//        for (ApComment apComment : list) {
//            ApCommentVo vo = new ApCommentVo();
//            BeanUtils.copyProperties(apComment, vo);
//            Query query1 = Query.query(Criteria.where("commentId").is(apComment.getId()).and("authorId").is(user.getId()));
//            ApCommentLike one = mongoTemplate.findOne(query1, ApCommentLike.class);
//            if (one != null) {
//                vo.setOperation((short) 0);
//            }
//            vos.add(vo);
//        }
////        5.2 遍历评论列表
////            将每一个评论  ApComment 转成 ApCommentVO
////          其中  如果当前评论id在点赞记录中存在  设置operation字段为0  不存在不用做任何处理
//
//        return ResponseResult.okResult(vos);
    }

    /**
     * 将comment 转为 vo对象   根据likes情况判断是否点过赞
     */
    private ApCommentVo parseCommentVO(ApComment apComment, List<String> commentIds) {
        ApCommentVo apCommentVo = new ApCommentVo();
        BeanUtils.copyProperties(apComment, apCommentVo);
        //遍历当前用户点赞列表
        if (commentIds.contains(apCommentVo.getId())) {
            apCommentVo.setOperation((short) 0);
        }
        return apCommentVo;
    }

    /**
     * 构建查询条件
     */
    private Query getQuery(CommentDTO dto, Integer size) {
        return Query.query(Criteria.where("articleId").is(dto.getArticleId())
                        .and("flag").is(0).and("createdTime").lt(dto.getMinDate()))
                .limit(size).with(Sort.by(Sort.Direction.DESC, "createdTime"));
    }

}
