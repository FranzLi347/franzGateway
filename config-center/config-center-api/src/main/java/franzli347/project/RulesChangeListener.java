package franzli347.project.register.center.impl;


/*
 * @Author: franzli 
 * @Date: 2024-04-23 08:15:32 
 * @Last Modified by: franzli
 * @Last Modified time: 2024-04-23 08:16:17
 */
public interface RulesChangeListener {
        /**
     * 规则变更时调用此方法 对规则进行更新
     * @param rules 新规则
     */
    void onRulesChange(List<Rule> rules);
}
