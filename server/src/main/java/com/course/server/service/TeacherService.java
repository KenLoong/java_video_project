package com.course.server.service;

import com.course.server.domain.Teacher;
import com.course.server.domain.TeacherExample;
import com.course.server.dto.TeacherDto;
import com.course.server.dto.PageDto;
import com.course.server.mapper.TeacherMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    /**
     * 列表查询
     */
    public List<TeacherDto> all() {
        TeacherExample teacherExample = new TeacherExample();
        List<Teacher> teacherList = teacherMapper.selectByExample(teacherExample);
        return CopyUtil.copyList(teacherList, TeacherDto.class);
    }

    public void list(PageDto pageDto){
        //这个会作用于后面一条select语句
        PageHelper.startPage(pageDto.getPage(),pageDto.getSize());
        TeacherExample teacherExample = new TeacherExample();
        List<Teacher> teacherList = teacherMapper.selectByExample(teacherExample);
        PageInfo<Teacher> pageInfo = new PageInfo<>(teacherList);
        pageDto.setTotal(pageInfo.getTotal());

        List<TeacherDto> teacherDtoList = new ArrayList<TeacherDto>();
        for (int i = 0; i < teacherList.size(); i++) {
            Teacher teacher = teacherList.get(i);
            TeacherDto teacherDto = new TeacherDto();
            BeanUtils.copyProperties(teacher,teacherDto);
            teacherDtoList.add(teacherDto);
        }
        pageDto.setList(teacherDtoList);
    }

    public void save(TeacherDto teacherDto){
        Teacher teacher = CopyUtil.copy(teacherDto, Teacher.class);
        if (StringUtils.isEmpty(teacherDto.getId())) {
            this.insert(teacher);
        } else {
            this.update(teacher);
        }
    }

    /**
     * 新增
     */
    private void insert(Teacher teacher) {
        teacher.setId(UuidUtil.getShortUuid());
        teacherMapper.insert(teacher);
    }

    /**
     * 更新
     */
    private void update(Teacher teacher) {
        teacherMapper.updateByPrimaryKey(teacher);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        teacherMapper.deleteByPrimaryKey(id);
    }

    /**
     * 查找
     * @param id
     */
    public TeacherDto findById(String id) {
        Teacher teacher = teacherMapper.selectByPrimaryKey(id);
        return CopyUtil.copy(teacher, TeacherDto.class);
    }


}
