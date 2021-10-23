package com.course.server.service;

import com.course.server.domain.RoleUser;
import com.course.server.domain.RoleUserExample;
import com.course.server.dto.RoleUserDto;
import com.course.server.dto.PageDto;
import com.course.server.mapper.RoleUserMapper;
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
public class RoleUserService {

    @Resource
    private RoleUserMapper roleUserMapper;


    public void list(PageDto pageDto){
        //这个会作用于后面一条select语句
        PageHelper.startPage(pageDto.getPage(),pageDto.getSize());
        RoleUserExample roleUserExample = new RoleUserExample();
        List<RoleUser> roleUserList = roleUserMapper.selectByExample(roleUserExample);
        PageInfo<RoleUser> pageInfo = new PageInfo<>(roleUserList);
        pageDto.setTotal(pageInfo.getTotal());

        List<RoleUserDto> roleUserDtoList = new ArrayList<RoleUserDto>();
        for (int i = 0; i < roleUserList.size(); i++) {
            RoleUser roleUser = roleUserList.get(i);
            RoleUserDto roleUserDto = new RoleUserDto();
            BeanUtils.copyProperties(roleUser,roleUserDto);
            roleUserDtoList.add(roleUserDto);
        }
        pageDto.setList(roleUserDtoList);
    }

    public void save(RoleUserDto roleUserDto){
        RoleUser roleUser = CopyUtil.copy(roleUserDto, RoleUser.class);
        if (StringUtils.isEmpty(roleUserDto.getId())) {
            this.insert(roleUser);
        } else {
            this.update(roleUser);
        }
    }

    /**
     * 新增
     */
    private void insert(RoleUser roleUser) {
        roleUser.setId(UuidUtil.getShortUuid());
        roleUserMapper.insert(roleUser);
    }

    /**
     * 更新
     */
    private void update(RoleUser roleUser) {
        roleUserMapper.updateByPrimaryKey(roleUser);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        roleUserMapper.deleteByPrimaryKey(id);
    }



}
