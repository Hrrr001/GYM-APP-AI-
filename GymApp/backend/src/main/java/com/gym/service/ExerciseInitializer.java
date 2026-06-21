package com.gym.service;

import com.gym.entity.Exercise;
import com.gym.repository.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ExerciseInitializer implements CommandLineRunner {

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已有标准动作，如果没有则初始化
        long count = exerciseRepository.countByIsCustomFalse();
        if (count == 0) {
            initializeStandardExercises();
        }
    }

    private void initializeStandardExercises() {
        List<Exercise> exercises = new ArrayList<>();

        // 胸部训练
        exercises.add(createExercise(
                "卧推",
                "胸部",
                "杠铃",
                "intermediate",
                "卧推是一种基础的胸部训练动作，主要锻炼胸大肌、三角肌前束和肱三头肌。",
                "1. 仰卧在卧推凳上，双脚平放在地面上\n2. 双手握杠，宽度略宽于肩\n3. 控制杠铃缓慢下降到胸部\n4. 推起杠铃至手臂伸直\n5. 重复动作",
                "保持核心收紧，动作要平稳",
                "避免腰部过度拱起，如有肩部不适请停止"
        ));

        exercises.add(createExercise(
                "哑铃飞鸟",
                "胸部",
                "哑铃",
                "beginner",
                "哑铃飞鸟主要锻炼胸肌外侧和中缝，是塑造胸肌线条的有效动作。",
                "1. 仰卧在卧推凳上，手持哑铃\n2. 手臂微屈，哑铃垂直向上\n3. 缓慢向两侧打开手臂\n4. 感受到胸肌拉伸后，缓慢收回\n5. 重复动作",
                "保持手臂微屈，动作要缓慢",
                "避免过度拉伸肩关节"
        ));

        // 背部训练
        exercises.add(createExercise(
                "引体向上",
                "背部",
                "单杠",
                "intermediate",
                "引体向上是锻炼背部肌肉的经典动作，主要锻炼背阔肌。",
                "1. 双手握住单杠，宽度与肩同宽\n2. 身体悬垂，双腿交叉\n3. 收缩背部肌肉，将身体向上拉\n4. 下巴超过单杠后，缓慢下放\n5. 重复动作",
                "保持核心收紧，避免摆动身体",
                "如果无法完成全程，可以使用助力带"
        ));

        exercises.add(createExercise(
                "硬拉",
                "背部",
                "杠铃",
                "advanced",
                "硬拉是一种复合训练动作，锻炼背部、臀部和腿部的多个肌群。",
                "1. 双脚与肩同宽，杠铃放在脚前\n2. 弯腰屈膝，双手握杠\n3. 背部挺直，缓慢提起杠铃\n4. 站直后，缓慢下放杠铃\n5. 重复动作",
                "保持背部挺直，动作要平稳",
                "避免腰部过度弯曲，如有腰部不适请停止"
        ));

        // 腿部训练
        exercises.add(createExercise(
                "深蹲",
                "腿部",
                "杠铃",
                "intermediate",
                "深蹲是锻炼腿部肌肉的基础动作，主要锻炼股四头肌、臀大肌和腘绳肌。",
                "1. 双脚与肩同宽，杠铃放在肩上\n2. 保持背部挺直，缓慢下蹲\n3. 大腿与地面平行后，起身\n4. 重复动作",
                "保持核心收紧，膝盖不要内扣",
                "避免膝盖超过脚尖，如有腰部不适请停止"
        ));

        exercises.add(createExercise(
                "腿举",
                "腿部",
                "器械",
                "beginner",
                "腿举是一种安全的腿部训练动作，适合初学者。",
                "1. 坐在腿举机上，双脚放在踏板上\n2. 缓慢推起踏板\n3. 控制下放，直到腿部接近90度\n4. 重复动作",
                "保持背部贴紧椅背，动作要平稳",
                "避免锁定膝盖"
        ));

        // 肩部训练
        exercises.add(createExercise(
                "哑铃肩上推举",
                "肩部",
                "哑铃",
                "intermediate",
                "哑铃肩上推举主要锻炼三角肌前束和中束。",
                "1. 双脚与肩同宽，手持哑铃\n2. 哑铃举至肩部高度\n3. 向上推举哑铃至手臂伸直\n4. 缓慢下放哑铃\n5. 重复动作",
                "保持核心收紧，动作要平稳",
                "避免腰部过度拱起"
        ));

        exercises.add(createExercise(
                "侧平举",
                "肩部",
                "哑铃",
                "beginner",
                "侧平举主要锻炼三角肌中束，是塑造肩部线条的有效动作。",
                "1. 双脚与肩同宽，手持哑铃\n2. 手臂微屈，缓慢向两侧抬起\n3. 达到肩部高度后，缓慢下放\n4. 重复动作",
                "保持手臂微屈，动作要缓慢",
                "避免使用过重的重量"
        ));

        // 手臂训练
        exercises.add(createExercise(
                "二头弯举",
                "手臂",
                "哑铃",
                "beginner",
                "二头弯举主要锻炼肱二头肌。",
                "1. 双脚与肩同宽，手持哑铃\n2. 手臂自然下垂\n3. 弯曲手臂，将哑铃举至肩部\n4. 缓慢下放哑铃\n5. 重复动作",
                "保持肘部固定，动作要缓慢",
                "避免摆动身体"
        ));

        exercises.add(createExercise(
                "三头下压",
                "手臂",
                "器械",
                "beginner",
                "三头下压主要锻炼肱三头肌。",
                "1. 坐在器械前，双手握住手柄\n2. 手臂伸直，缓慢向下压手柄\n3. 感受到三头肌收缩后，缓慢收回\n4. 重复动作",
                "保持背部挺直，动作要平稳",
                "避免锁定肘部"
        ));

        // 核心训练
        exercises.add(createExercise(
                "平板支撑",
                "核心",
                "无器械",
                "beginner",
                "平板支撑是一种有效的核心训练动作，锻炼腹部和背部肌肉。",
                "1. 俯卧，肘部支撑在地面上\n2. 双脚并拢，身体保持一条直线\n3. 保持这个姿势，尽可能长时间\n4. 放松休息",
                "保持核心收紧，不要塌腰",
                "如有腰部不适请停止"
        ));

        exercises.add(createExercise(
                "仰卧起坐",
                "核心",
                "无器械",
                "beginner",
                "仰卧起坐主要锻炼腹肌。",
                "1. 仰卧在地面上，膝盖弯曲\n2. 双手交叉放在胸前\n3. 收缩腹肌，将上半身抬起\n4. 缓慢下放\n5. 重复动作",
                "保持动作缓慢，避免使用惯性",
                "如有颈部不适请停止"
        ));

        // 保存所有动作
        exerciseRepository.saveAll(exercises);
        System.out.println("标准健身动作初始化完成，共添加 " + exercises.size() + " 个动作");
    }

    private Exercise createExercise(String name, String category, String equipment, String difficulty, 
                                   String description, String instructions, String tips, String precautions) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setCategory(category);
        exercise.setEquipment(equipment);
        exercise.setDifficulty(difficulty);
        exercise.setDescription(description);
        exercise.setInstructions(instructions);
        exercise.setTips(tips);
        exercise.setPrecautions(precautions);
        exercise.setCustom(false);
        return exercise;
    }
}
