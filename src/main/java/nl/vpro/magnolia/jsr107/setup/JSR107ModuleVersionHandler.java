/*
 * Copyright (C) 2017 All rights reserved
 * VPRO The Netherlands
 */
package nl.vpro.magnolia.jsr107.setup;

import info.magnolia.module.DefaultModuleVersionHandler;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;

import java.util.Arrays;
import java.util.List;

/**
 * @author michiel
 */
public class JSR107ModuleVersionHandler extends DefaultModuleVersionHandler {

    public JSR107ModuleVersionHandler() {
    }

    @Override
    protected List<Task> getExtraInstallTasks(InstallContext installContext) {
        return Arrays.asList();
    }

}
