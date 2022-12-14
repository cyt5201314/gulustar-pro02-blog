package gulustar.service.impl;

import gulustar.mapper.BlogMapper;
import gulustar.pojo.Blog;
import gulustar.pojo.BlogPageBean;
import gulustar.pojo.Comment;
import gulustar.pojo.Conditions;
import gulustar.service.BlogService;

import gulustar.util.SqlSessionFactoryUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.*;
import java.util.List;

/**
 * @author spp
 * @create 2022-08-12 16:13
 */
public class BlogServiceImpl implements BlogService{
    SqlSessionFactory sqlSessionFactory = SqlSessionFactoryUtils.getSqlSessionFactory();

    /**
     * 添加博客
     * @return
     */
    @Override
    public boolean addBlog(Blog blog) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

        boolean addBlog = blogMapper.addBlog(blog);

        sqlSession.commit();
        sqlSession.close();
        return addBlog;
    }

    /**
     * 添加一条评论 对象里包含博客，用户ID 和 评论内容
     * @param comment
     * @return
     */
    @Override
    public boolean addComment(Comment comment) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

        //添加评论到评论表
        boolean addComment = blogMapper.addComment(comment);
        //获取自增值 = 新增博客的ID
        Integer commentId = blogMapper.selectLastInsertId();
        //添加2个ID到blog_comment表，建立对应关系
        Integer blogId = comment.getBlogId();
        boolean addCommentRelation = blogMapper.addCommentRelation(blogId, commentId);

        sqlSession.commit();
        sqlSession.close();
        return addComment && addCommentRelation;
    }

    /**
     * 获取指定ID博客 包含博客内容和评论
     * @param id
     * @return
     */
    @Override
    public Blog getOneBlog(Integer id) throws IOException {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper blogMapper = sqlSession.getMapper(BlogMapper.class);

        //查询博客内容和评论集合,把评论集合封装到博客对象里
        Blog blog = blogMapper.selectOneBlog(id);
        //获取文件地址 数据库存的是文件地址
        String path = blog.getContent();
        //获取文件内容 并设置到content属性
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        InputStreamReader reader = new InputStreamReader(fileInputStream);
        BufferedReader buffReader = new BufferedReader(reader);
        String strTmp = "";
        StringBuilder content = new StringBuilder();
        while((strTmp = buffReader.readLine()) != null){
            System.out.println(strTmp);
            content.append(strTmp);
        }
        buffReader.close();
        blog.setContent(content.toString());
        //获取存在数据库的评论ID集合  然后查询评论并设置到属性
        List<Integer> commentIds = blogMapper.selectCommentIdList(id);
        List<Comment> comments = blogMapper.selectCommentsByIds(commentIds);
        blog.setComment(comments);

        sqlSession.close();
        return blog;
    }

    /**
     * 根据条件查询博客
     * @return
     */
    @Override
    public BlogPageBean getBlogsByPageAndCondition(Conditions conditions) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

        //计算limit的开始位置
        Integer currentPage = conditions.getCurrentPage();
        Integer size = conditions.getSize();
        int start = (currentPage - 1) * size;

        //获取符合条件博客集合
        List<Blog> blogs = mapper.selectByPageAndCondition(conditions, start, size);
        //计算总页数
        Integer count = mapper.selectCountByCondition(conditions);
        Integer totalPage = count / size;
        totalPage = (totalPage * size < count)? totalPage + 1: totalPage;

        //封装为pageBean对象
        BlogPageBean pageBean = new BlogPageBean();
        pageBean.setBlogs(blogs);
        pageBean.setTotal(totalPage);

        sqlSession.close();
        return pageBean;
    }

    /**
     * 根据分类ID查询：返回博客对象数组
     * @param category
     * @return
     */
    @Override
    public List<Blog> queryByCategory(String category) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

        List<Blog> blogs = mapper.selectByCategory(category);
        sqlSession.close();
        return blogs;
    }

    /**
     * 根据关键词查询
     * @param keyword
     * @return
     */
    @Override
    public List<Blog> queryByKeyword(String keyword) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

        List<Blog> blogs = mapper.selectByKeyword(keyword);
        sqlSession.close();
        return blogs;
    }

    /**
     * 根据用户id查询收藏的博客
     * @param
     * @return
     */
    @Override
    public BlogPageBean selectCollect(Integer userId, Conditions conditions) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

        //设置查询条件
        Integer page = conditions.getCurrentPage();
        Integer size = conditions.getSize();
        int start = (page - 1) * size;
        conditions.setCurrentPage(start);

        //获取收藏的博客集合
        List<Blog> blogs = mapper.selectCollect(userId, conditions);
        //获取收藏博客总数 计算页数
        Integer count = mapper.selectCollectCount(userId, conditions);
        Integer totalPage = count / size;
        totalPage = (totalPage * size < count)? totalPage + 1: totalPage;

        //封装为pageBean对象
        BlogPageBean pageBean = new BlogPageBean();
        pageBean.setBlogs(blogs);
        pageBean.setTotal(totalPage);

        sqlSession.close();
        return pageBean;
    }

    /**
     * 获取全部分类
     * @return
     */
    @Override
    public List<String> getAllCategories() {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

        List<String> categories = mapper.selectAllCategories();

        sqlSession.close();
        return categories;
    }

    /**
     * 增加点赞数
     * @param blogId
     * @return
     */
    @Override
    public boolean addLikes(Integer blogId) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        BlogMapper mapper = sqlSession.getMapper(BlogMapper.class);

        boolean addOK = mapper.addLikes(blogId);

        sqlSession.commit();
        sqlSession.close();
        return addOK;
    }
}
