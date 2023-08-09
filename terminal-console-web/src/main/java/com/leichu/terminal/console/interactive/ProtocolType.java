package com.leichu.terminal.console.interactive;


import com.leichu.terminal.console.interactive.model.Protocol;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ProtocolType {

	Protocol type();

}
