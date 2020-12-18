/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.intellij.serviceexplorer;

import com.google.common.collect.ImmutableList;
import com.microsoft.intellij.helpers.AzureAllIcons;
import com.microsoft.tooling.msservices.serviceexplorer.Node;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLModule;
import com.microsoft.tooling.msservices.serviceexplorer.azure.mysql.MySQLNode;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public class NodeIconsMap {

    public static final Map<Class<? extends Node>, Icon> NODE_TO_ICONS = new HashMap<>();
    public static final Map<Class<? extends Node>, ImmutableList<Icon>> NODE_TO_STATE_ICONS = new HashMap<>();

    static {
        NODE_TO_ICONS.put(MySQLModule.class, AzureAllIcons.MySQL.MODULE);

        NODE_TO_STATE_ICONS.put(MySQLNode.class, new ImmutableList.Builder<Icon>()
                .add(AzureAllIcons.MySQL.RUNNING, AzureAllIcons.MySQL.STOPPED, AzureAllIcons.MySQL.UPDATING).build());
    }
}
