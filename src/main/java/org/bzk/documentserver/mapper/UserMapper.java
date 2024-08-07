package org.bzk.documentserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.bzk.documentserver.bean.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
