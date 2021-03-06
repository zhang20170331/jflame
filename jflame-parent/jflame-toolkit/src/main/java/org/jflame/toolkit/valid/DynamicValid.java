package org.jflame.toolkit.valid;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 动态验证注解,使用指定的内置验证规则验证
 * 
 * @author yucan.zhang
 */
@Constraint(validatedBy = { DynamicValidator.class })
@Documented
@Target({ ElementType.ANNOTATION_TYPE,ElementType.METHOD,ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface DynamicValid {

    /**
     * 错误描述,如需引用params中的动态参数,使用{0},{1}占用符
     * 
     * @return
     */
    String message();

    /**
     * 指定验证规则,多条规则是and关系
     * 
     * @return
     */
    ValidRule[] rules();

    /**
     * 校验分组
     * 
     * @return
     */
    Class<?>[] groups() default {};

    /**
     * 用于同一属性不同条件时的验证区分
     * 
     * @return
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * 是否可用null 默认false
     * 
     * @return
     */
    boolean nullable() default false;

    /**
     * 规则所需参数,格式说明:<br>
     * 规则名1:参数,规则名:参数. 如:rules={ValidRule.min,ValidRule.regex} param="min:1,regex:\"\\\\d{1,13}\""; 参数表示
     * 数字:1,字符串:"1",数组:[1,2]
     * 
     * @return
     */
    String params() default "";

    /**
     * 内置验证规则
     * 
     * @author yucan.zhang
     */
    public enum ValidRule {
        /**
         * 手机
         */
        mobile,
        /**
         * 电话号
         */
        tel,
        /**
         * 电话或手机号
         */
        mobileOrTel,
        /**
         * 身份证验证
         */
        idcard,
        /**
         * 字母
         */
        letter,
        /**
         * 字母,数字或下划线
         */
        letterNumOrline,
        /**
         * ip地址
         */
        ip,
        /**
         * 不包含特殊字符*%#\=&lt;&gt;`';?&amp;!
         */
        safeChar,
        noContain,
        size,
        min,
        max,
        regex
    }

}
