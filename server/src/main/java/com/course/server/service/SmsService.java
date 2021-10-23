package com.course.server.service;

import com.course.server.domain.Sms;
import com.course.server.domain.SmsExample;
import com.course.server.dto.SmsDto;
import com.course.server.dto.PageDto;
import com.course.server.enums.SmsStatusEnum;
import com.course.server.exception.BusinessException;
import com.course.server.exception.BusinessExceptionCode;
import com.course.server.mapper.SmsMapper;
import com.course.server.util.CopyUtil;
import com.course.server.util.MailUtil;
import com.course.server.util.UuidUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
        import java.util.Date;

@Service
public class SmsService {

    private static final Logger LOG = LoggerFactory.getLogger(SmsService.class);

    @Resource
    private SmsMapper smsMapper;

    @Autowired
    private MailUtil mailUtil;


    public void list(PageDto pageDto){
        //这个会作用于后面一条select语句
        PageHelper.startPage(pageDto.getPage(),pageDto.getSize());
        SmsExample smsExample = new SmsExample();
        List<Sms> smsList = smsMapper.selectByExample(smsExample);
        PageInfo<Sms> pageInfo = new PageInfo<>(smsList);
        pageDto.setTotal(pageInfo.getTotal());

        List<SmsDto> smsDtoList = new ArrayList<SmsDto>();
        for (int i = 0; i < smsList.size(); i++) {
            Sms sms = smsList.get(i);
            SmsDto smsDto = new SmsDto();
            BeanUtils.copyProperties(sms,smsDto);
            smsDtoList.add(smsDto);
        }
        pageDto.setList(smsDtoList);
    }

    public void save(SmsDto smsDto){
        Sms sms = CopyUtil.copy(smsDto, Sms.class);
        if (StringUtils.isEmpty(smsDto.getId())) {
            this.insert(sms);
        } else {
            this.update(sms);
        }
    }

    /**
     * 新增
     */
    private void insert(Sms sms) {
                Date now = new Date();
        sms.setId(UuidUtil.getShortUuid());
        smsMapper.insert(sms);
    }

    /**
     * 更新
     */
    private void update(Sms sms) {
        smsMapper.updateByPrimaryKey(sms);
    }

    /**
     * 删除
     */
    public void delete(String id) {
        smsMapper.deleteByPrimaryKey(id);
    }


    /**
     * 发送短信验证码
     * 同手机号同操作1分钟内不能重复发送短信
     * @param smsDto
     */
    public void sendCode(SmsDto smsDto) throws MessagingException {
        SmsExample example = new SmsExample();
        SmsExample.Criteria criteria = example.createCriteria();
        // 查找1分钟内有没有同手机号同操作发送记录且没被用过
        criteria.andMobileEqualTo(smsDto.getMobile())
                .andUseEqualTo(smsDto.getUse())
                .andStatusEqualTo(SmsStatusEnum.NOT_USED.getCode())
                //限制时间，1分钟内
                .andAtGreaterThan(new Date(new Date().getTime() - 1 * 60 * 1000));
        List<Sms> smsList = smsMapper.selectByExample(example);

        if (smsList == null || smsList.size() == 0) {

            //校验通过，发送短信
            saveAndSend(smsDto);
        } else {
            LOG.warn("短信请求过于频繁, {}", smsDto.getMobile());
            throw new BusinessException(BusinessExceptionCode.MOBILE_CODE_TOO_FREQUENT);
        }
    }

    /**
     * 保存并发送邮件验证码，因为短信服务开通要求需要企业资格
     * @param smsDto
     */
    private void saveAndSend(SmsDto smsDto) throws MessagingException {
        // 生成6位数字
        String code = String.valueOf((int)(((Math.random() * 9) + 1) * 100000));
        smsDto.setAt(new Date());
        smsDto.setStatus(SmsStatusEnum.NOT_USED.getCode());
        smsDto.setCode(code);
        this.save(smsDto);

        // TODO 调第三方接口发送邮件
        mailUtil.send(smsDto);

    }

    /**
     * 验证码5分钟内有效，且操作类型要一致
     * @param smsDto
     */
    public void validCode(SmsDto smsDto) {
        SmsExample example = new SmsExample();
        SmsExample.Criteria criteria = example.createCriteria();
        // 查找5分钟内同邮箱同操作发送记录
        criteria.andMobileEqualTo(smsDto.getMobile()).andUseEqualTo(smsDto.getUse()).andAtGreaterThan(new Date(new Date().getTime() - 5 * 60 * 1000));
        List<Sms> smsList = smsMapper.selectByExample(example);

        if (smsList != null && smsList.size() > 0) {
            Sms smsDb = smsList.get(0);
            if (!smsDb.getCode().equals(smsDto.getCode())) {
                LOG.warn("邮箱验证码不正确");
                throw new BusinessException(BusinessExceptionCode.MOBILE_CODE_ERROR);
            } else {
                //查找到验证码了
                smsDto.setStatus(SmsStatusEnum.USED.getCode());
                smsMapper.updateByPrimaryKey(smsDb);
            }
        } else {
            LOG.warn("邮箱验证码不存在或已过期，请重新发送短信");
            throw new BusinessException(BusinessExceptionCode.MOBILE_CODE_EXPIRED);
        }
    }

}
