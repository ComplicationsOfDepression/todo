package com.todo.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.todo.anno.Log;
import com.todo.common.Result;
import com.todo.common.Status;
import com.todo.pojo.TbTask;
import com.todo.service.TbTaskService;
import com.todo.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * <p>
 * 任务表 前端控制器
 * </p>
 *
 * @author dayuan
 * @since 2023-12-26
 */
@RestController
@RequestMapping("/tb-task")
public class TbTaskController {

    @Autowired
    private TbTaskService tbTaskService;

    @GetMapping("/taskNumber")
    public Result selectTaskNumber(){
        try {
            List<Map<String,Object>> taskCounts=tbTaskService.listMaps(
                    new QueryWrapper<TbTask>().groupBy("state").select("state,count(*) as count")
            );
            Map<String,Integer> result=new HashMap<>();
            for (Map<String,Object> taskCount:taskCounts){
                String state = taskCount.get("state").toString();
                Integer count = Integer.parseInt(taskCount.get("count").toString());
                // 判断状态是否为4、5、6，合并为key4
                if ("4".equals(state) || "5".equals(state) || "6".equals(state)) {
                    state = "4";
                }

                // 将结果相加
                result.put(state, result.getOrDefault(state, 0) + count);
            }
            return Result.buildR(Status.OK,"result",result);
        } catch (NumberFormatException e) {
            return Result.buildR(Status.SQL_ERROR,"查询失败",e.getMessage());
        }
    }

    @GetMapping("/taskByTime")
    public Result selectTaskByTime(){
        try {
            List<Map<String,Object>> taskCounts=tbTaskService.listMaps(
                    new QueryWrapper<TbTask>().groupBy("state").select("state,count(*) as count")
            );
            Map<String,Integer> result=new HashMap<>();
            for (Map<String,Object> taskCount:taskCounts) {
                String state = taskCount.get("state").toString();
                Integer count = Integer.parseInt(taskCount.get("count").toString());
                result.put(state, count);
            }
            return Result.buildR(Status.OK,"result",result);
        } catch (NumberFormatException e) {
            return Result.buildR(Status.SQL_ERROR,"查询失败",e.getMessage());
        }
    }

    @GetMapping("/taskByWeek")
    public Result selectTaskByWeek() {
        try {
            // 获取当前日期和本周一的日期
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            //String currentDate = sdf.format(calendar.getTime());

            // 获取本周一的日期
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            String currentDate = sdf.format(calendar.getTime());

            // 获取下一周的星期日的日期
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            String nextSunday = sdf.format(calendar.getTime());


            // 日期分组，统计数量
            List<Map<String, Object>> taskCounts = tbTaskService.listMaps(
                    new QueryWrapper<TbTask>()
                            .ge("create_time", currentDate)
                            .le("create_time", nextSunday)
                            .groupBy("date_format(create_time,'%w') ")
                            .select("date_format(create_time,'%w') as dayOfWeek,count(*) as count")
            );

            // 封装结果，确保每一天都在结果中，不存在的日期数量为0
            Map<String, Integer> result = new LinkedHashMap<>();
            for (int i = 0; i <= 6; i++) {
                String actualDayOfWeek = getActualDayOfWeek(i);
                result.put(actualDayOfWeek, 0);
            }

            for (Map<String, Object> taskCount : taskCounts) {
                String dayOfWeek = taskCount.get("dayOfWeek").toString();
                Integer count = Integer.parseInt(taskCount.get("count").toString());
                String actualDayOfWeek = getActualDayOfWeek(Integer.parseInt(dayOfWeek));
                result.put(actualDayOfWeek, count);
            }

            System.out.println("Current Date: " + currentDate);
            System.out.println("Next Sunday: " + nextSunday);
            return Result.buildR(Status.OK, "result", result);

        } catch (NumberFormatException e) {
            return Result.buildR(Status.SQL_ERROR, "查询失败", e.getMessage());
        }
    }

    private String getActualDayOfWeek(int dayOfWeek) {
        String[] days = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
        // 将dayOfWeek减一，以适应数组的索引范围
        return days[dayOfWeek];
    }

    @GetMapping("/workTask")
    public Result selectWorkTask(@RequestBody TbTask tbTask) {
        try {
            // 先更新状态
            updateTaskState();

            // 执行查询
            QueryWrapper<TbTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("state", 1, 2, 3)
                    .eq("create_username", tbTask.getCreateUsername())
                    .eq("is_delete", 0);

            List<TbTask> tbTaskList = tbTaskService.list(queryWrapper);
            return Result.buildR(Status.OK, "task", tbTaskList);
        } catch (Exception e) {
            return Result.buildR(Status.SQL_ERROR, "查询失败");
        }
    }

    private void updateTaskState() {
        List<TbTask> taskList = tbTaskService.list(new QueryWrapper<>());

        LocalDateTime now = LocalDateTime.now();

        for (TbTask task : taskList) {
            String startTimeString = task.getStartTime();
            String endTimeString = task.getEndTime();
            if (startTimeString == null) {
                // 处理开始时间为null的情况
                continue;
            }

            LocalDateTime startTimeLocalDateTime = LocalDateTime.parse(startTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime endTimeLocalDateTime = LocalDateTime.parse(endTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if (now.isBefore(startTimeLocalDateTime)) {
                // 任务未开始
                task.setState("1"); // 设置为未开始的状态
            } else if (now.isAfter(endTimeLocalDateTime)) {
                // 任务已结束
                task.setState("3"); // 设置为超期的状态
            } else {
                // 任务进行中
                task.setState("2"); // 设置为进行中的状态
            }

            // 更新任务状态
            tbTaskService.updateById(task);
        }
    }




    @Log
    @PutMapping("/addTask")
    public Result addTask(@RequestBody TbTask tbTask, HttpServletRequest request){

        // 设置其他属性
        String title = tbTask.getTitle();
        String content = tbTask.getContent();
        String type = tbTask.getType();
        String startTime = tbTask.getStartTime();
        String endTime = tbTask.getEndTime();
        String remark = tbTask.getRemark();

        String jwt=request.getHeader("Token");
        Claims claims = JwtUtils.parseJWT(jwt);
        String id = claims.get("id", String.class);
        tbTask.setCreateUsername(id);
        tbTask.setIsDelete(0);

        boolean success = tbTaskService.save(tbTask);
        if (success) {
            return Result.buildR(Status.OK, "任务添加成功",tbTask);
        } else {
            return Result.buildR(Status.SQL_ERROR, "任务添加失败");
        }
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestBody TbTask taskId) {
        // 根据任务ID和 is_delete=0 从数据库中查询任务信息
        QueryWrapper<TbTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", taskId.getId())
                .eq("is_delete", 0);

        TbTask tbTask = tbTaskService.getOne(queryWrapper);

        return Result.buildR(Status.OK, tbTask);
    }

    @Log
    @PutMapping("/updateTask")
    public Result updateTask(@RequestBody TbTask tbTask){
        // 根据任务ID判断任务是否存在
        TbTask existingTask = tbTaskService.getById(tbTask.getId());
        if (existingTask == null) {
            return Result.buildR(Status.SYSTEM_ERROR, "任务不存在");
        }

        // 更新任务信息
        existingTask.setTitle(tbTask.getTitle());
        existingTask.setContent(tbTask.getContent());
        existingTask.setType(tbTask.getType());
        existingTask.setStartTime(tbTask.getStartTime());
        existingTask.setEndTime(tbTask.getEndTime());
        existingTask.setRemark(tbTask.getRemark());

        // 调用服务层的更新方法
        boolean success = tbTaskService.updateById(existingTask);
        return Result.buildR(Status.OK,tbTask);
    }

    @Log
    @DeleteMapping("/deleteTask")
    public Result deleteTask(@RequestBody List<String> taskIds) {
        try {
            TbTask updateEntity = new TbTask();
            updateEntity.setIsDelete(1); // 假设is_delete的setter方法为setIsDelete

            UpdateWrapper<TbTask> updateWrapper = new UpdateWrapper<>();
            updateWrapper.in("id", taskIds);

            // 根据任务ID列表更新 is_delete 字段的值为 1
            boolean success = tbTaskService.update(updateEntity, updateWrapper);

            if (success) {
                return Result.buildR(Status.OK, "更新成功");
            } else {
                return Result.buildR(Status.SYSTEM_ERROR, "更新失败");
            }
        } catch (Exception e) {
            return Result.buildR(Status.SQL_ERROR, "更新失败");
        }
    }

    @Log
    @PutMapping("/submitTask")
    public Result submitTask(@RequestBody TbTask taskId){
        try {
            TbTask task = tbTaskService.getById(taskId);
            if (task == null) {
                // 处理任务不存在的情况
                return Result.buildR(Status.SQL_ERROR,"处理任务不存在的情况");
            }
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endTimeLocalDateTime = LocalDateTime.parse(task.getEndTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDateTime startTimeLocalDateTime = LocalDateTime.parse(task.getStartTime(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            if ("3".equals(task.getState())) {
                // 如果任务状态为3（超期），则改为6（超期完成）
                task.setState("6"); // 设置为超期完成的状态
            } else if (now.isBefore(startTimeLocalDateTime)) {
                // 任务提前完成
                task.setState("4"); // 设置为提前完成的状态
            } else if (now.isEqual(endTimeLocalDateTime) || now.isBefore(endTimeLocalDateTime.plusMinutes(15))) {
                // 任务正常完成
                task.setState("5"); // 设置为正常完成的状态
            }

            // 更新完成时间
            task.setCompleteTime(String.valueOf(now));

            // 更新任务状态和完成时间
            tbTaskService.updateById(task);
            return Result.buildR(Status.OK, "Task submitted successfully");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/doneTask")
    public Result selectDoneTask(@RequestBody TbTask tbTask) {
        try {
            QueryWrapper<TbTask> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("state", 4, 5, 6)
                    .eq("create_username", tbTask.getCreateUsername())
                    .eq("is_delete", 0);

            List<TbTask> tbTaskList = tbTaskService.list(queryWrapper);
            return Result.buildR(Status.OK, "task", tbTaskList);
        } catch (Exception e) {
            return Result.buildR(Status.SQL_ERROR, "查询失败");
        }
    }



}

