package com.chenjie.mysql.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.chenjie.mysql.entity.Worker;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface WorkerMapper extends BaseMapper<Worker> {
    void insertWorkerBatch(List<Worker> workerList);
}
