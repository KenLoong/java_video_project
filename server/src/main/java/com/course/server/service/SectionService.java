package com.course.server.service;

import com.course.server.domain.Section;
import com.course.server.domain.SectionExample;
import com.course.server.dto.SectionDto;
import com.course.server.dto.PageDto;
import com.course.server.dto.SectionPageDto;
import com.course.server.enums.SectionChargeEnum;
import com.course.server.mapper.SectionMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
        import java.util.Date;

@Service
public class SectionService {

    @Resource
    private SectionMapper sectionMapper;

    @Resource
    private CourseService courseService;


    public void list(SectionPageDto sectionPageDto){
        //这个会作用于后面一条select语句
        PageHelper.startPage(sectionPageDto.getPage(),sectionPageDto.getSize());
        SectionExample sectionExample = new SectionExample();
        SectionExample.Criteria criteria = sectionExample.createCriteria();
        if (!StringUtils.isEmpty(sectionPageDto.getCourseId())) {
            criteria.andCourseIdEqualTo(sectionPageDto.getCourseId());
        }
        if (!StringUtils.isEmpty(sectionPageDto.getChapterId())) {
            criteria.andChapterIdEqualTo(sectionPageDto.getChapterId());
        }

        sectionExample.setOrderByClause("sort asc");
        List<Section> sectionList = sectionMapper.selectByExample(sectionExample);
        PageInfo<Section> pageInfo = new PageInfo<>(sectionList);
        sectionPageDto.setTotal(pageInfo.getTotal());

        List<SectionDto> sectionDtoList = new ArrayList<SectionDto>();
        for (int i = 0; i < sectionList.size(); i++) {
            Section section = sectionList.get(i);
            SectionDto sectionDto = new SectionDto();
            BeanUtils.copyProperties(section,sectionDto);
            sectionDtoList.add(sectionDto);
        }
        sectionPageDto.setList(sectionDtoList);
    }

    /**
     * 涉及到多表操作，增加事务管理，
     * 注意，因为Transactional默认只会对RuntimeException回滚事务
     * 所以加上rollbackFor = Exception.class
     * 使其对所有异常都回滚
     */
    @Transactional(rollbackFor = Exception.class)
    public void save(SectionDto sectionDto){
        Section section = CopyUtil.copy(sectionDto, Section.class);
        if (StringUtils.isEmpty(sectionDto.getId())) {
            this.insert(section);
        } else {
            this.update(section);
        }
        courseService.updateTime(sectionDto.getCourseId());
    }

    /**
     * 新增
     */
    private void insert(Section section) {
        Date now = new Date();
        section.setCreatedAt(now);
        section.setUpdatedAt(now);
        section.setId(UuidUtil.getShortUuid());
        //默认免费
        section.setCharge(SectionChargeEnum.FREE.getCode());
        sectionMapper.insert(section);
    }

    /**
     * 更新
     */
    private void update(Section section) {
                section.setUpdatedAt(new Date());
        sectionMapper.updateByPrimaryKey(section);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        sectionMapper.deleteByPrimaryKey(id);
    }


    /**
     * 查询某一课程下的所有节
     */
    public List<SectionDto> listByCourse(String courseId) {
        SectionExample example = new SectionExample();
        example.createCriteria().andCourseIdEqualTo(courseId);
        List<Section> sectionList = sectionMapper.selectByExample(example);
        List<SectionDto> sectionDtoList = CopyUtil.copyList(sectionList, SectionDto.class);
        return sectionDtoList;
    }

}
