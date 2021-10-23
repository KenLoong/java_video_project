package com.course.server.service;

import com.course.server.domain.RoleResource;
import com.course.server.domain.RoleResourceExample;
import com.course.server.dto.RoleResourceDto;
import com.course.server.dto.PageDto;
import com.course.server.mapper.RoleResourceMapper;
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
public class RoleResourceService {

    @Resource
    private RoleResourceMapper roleResourceMapper;


    public void list(PageDto pageDto){
        //这个会作用于后面一条select语句
        PageHelper.startPage(pageDto.getPage(),pageDto.getSize());
        RoleResourceExample roleResourceExample = new RoleResourceExample();
        List<RoleResource> roleResourceList = roleResourceMapper.selectByExample(roleResourceExample);
        PageInfo<RoleResource> pageInfo = new PageInfo<>(roleResourceList);
        pageDto.setTotal(pageInfo.getTotal());

        List<RoleResourceDto> roleResourceDtoList = new ArrayList<RoleResourceDto>();
        for (int i = 0; i < roleResourceList.size(); i++) {
            RoleResource roleResource = roleResourceList.get(i);
            RoleResourceDto roleResourceDto = new RoleResourceDto();
            BeanUtils.copyProperties(roleResource,roleResourceDto);
            roleResourceDtoList.add(roleResourceDto);
        }
        pageDto.setList(roleResourceDtoList);
    }

    public void save(RoleResourceDto roleResourceDto){
        RoleResource roleResource = CopyUtil.copy(roleResourceDto, RoleResource.class);
        if (StringUtils.isEmpty(roleResourceDto.getId())) {
            this.insert(roleResource);
        } else {
            this.update(roleResource);
        }
    }

    /**
     * 新增
     */
    private void insert(RoleResource roleResource) {
        roleResource.setId(UuidUtil.getShortUuid());
        roleResourceMapper.insert(roleResource);
    }

    /**
     * 更新
     */
    private void update(RoleResource roleResource) {
        roleResourceMapper.updateByPrimaryKey(roleResource);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        roleResourceMapper.deleteByPrimaryKey(id);
    }



}
