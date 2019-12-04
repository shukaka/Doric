/*
 * Copyright [2019] [Doric.Pub]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.doric.plugin;

import com.github.pengfeizhou.jscore.ArchiveException;
import com.github.pengfeizhou.jscore.JSDecoder;
import com.github.pengfeizhou.jscore.JSObject;

import pub.doric.DoricContext;
import pub.doric.extension.bridge.DoricMethod;
import pub.doric.extension.bridge.DoricPlugin;
import pub.doric.navigator.IDoricNavigator;
import pub.doric.utils.ThreadMode;

/**
 * @Description: pub.doric.plugin
 * @Author: pengfei.zhou
 * @CreateDate: 2019-11-23
 */
@DoricPlugin(name = "navigator")
public class NavigatorPlugin extends DoricJavaPlugin {
    public NavigatorPlugin(DoricContext doricContext) {
        super(doricContext);
    }

    @DoricMethod(thread = ThreadMode.UI)
    public void push(JSDecoder jsDecoder) {
        IDoricNavigator navigator = getDoricContext().getDoricNavigator();
        if (navigator != null) {
            try {
                JSObject jsObject = jsDecoder.decode().asObject();
                navigator.push(jsObject.getProperty("scheme").asString().value(),
                        jsObject.getProperty("alias").asString().value()
                );
            } catch (ArchiveException e) {
                e.printStackTrace();
            }
        }
    }

    @DoricMethod(thread = ThreadMode.UI)
    public void pop() {
        IDoricNavigator navigator = getDoricContext().getDoricNavigator();
        if (navigator != null) {
            navigator.pop();
        }
    }
}
