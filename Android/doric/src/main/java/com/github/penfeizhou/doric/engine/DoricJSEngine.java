package com.github.penfeizhou.doric.engine;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.github.penfeizhou.doric.DoricRegistry;
import com.github.penfeizhou.doric.extension.bridge.DoricBridgeExtension;
import com.github.penfeizhou.doric.extension.timer.DoricTimerExtension;
import com.github.penfeizhou.doric.utils.DoricConstant;
import com.github.penfeizhou.doric.utils.DoricLog;
import com.github.penfeizhou.doric.utils.DoricUtils;
import com.github.pengfeizhou.jscore.JSDecoder;
import com.github.pengfeizhou.jscore.JavaFunction;
import com.github.pengfeizhou.jscore.JavaValue;

import java.util.ArrayList;

/**
 * @Description: Doric
 * @Author: pengfei.zhou
 * @CreateDate: 2019-07-18
 */
public class DoricJSEngine implements Handler.Callback, DoricTimerExtension.TimerCallback {
    private final Handler mJSHandler;
    private final DoricBridgeExtension mDoricBridgeExtension = new DoricBridgeExtension();
    private IDoricJSE mDoricJSE;
    private final DoricTimerExtension mTimerExtension;
    private final DoricRegistry mDoricRegistry = new DoricRegistry();

    public DoricJSEngine() {
        HandlerThread handlerThread = new HandlerThread(this.getClass().getSimpleName());
        handlerThread.start();
        Looper looper = handlerThread.getLooper();
        mJSHandler = new Handler(looper, this);
        mJSHandler.post(new Runnable() {
            @Override
            public void run() {
                initJSExecutor();
                initHugoRuntime();
            }
        });
        mTimerExtension = new DoricTimerExtension(looper, this);
    }

    public Handler getJSHandler() {
        return mJSHandler;
    }


    private void initJSExecutor() {
        mDoricJSE = new DoricJSExecutor();
        mDoricJSE.injectGlobalJSFunction(DoricConstant.INJECT_LOG, new JavaFunction() {
            @Override
            public JavaValue exec(JSDecoder[] args) {
                try {
                    String type = args[0].string();
                    String message = args[1].string();
                    switch (type) {
                        case "w":
                            DoricLog.suffix_w("_js", message);
                            break;
                        case "e":
                            DoricLog.suffix_e("_js", message);
                            break;
                        default:
                            DoricLog.suffix_d("_js", message);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        mDoricJSE.injectGlobalJSFunction(DoricConstant.INJECT_REQUIRE, new JavaFunction() {
            @Override
            public JavaValue exec(JSDecoder[] args) {
                try {
                    String name = args[0].string();
                    String content = mDoricRegistry.acquireJSBundle(name);
                    if (TextUtils.isEmpty(content)) {
                        DoricLog.e("require js bundle:%s is empty", name);
                        return new JavaValue(false);
                    }
                    mDoricJSE.loadJS(packageModuleScript(name, content), "Module://" + name);
                    return new JavaValue(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    return new JavaValue(false);
                }
            }
        });
        mDoricJSE.injectGlobalJSFunction(DoricConstant.INJECT_TIMER_SET, new JavaFunction() {
            @Override
            public JavaValue exec(JSDecoder[] args) {
                try {
                    mTimerExtension.setTimer(
                            args[0].number().longValue(),
                            args[1].number().longValue(),
                            args[2].bool());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        mDoricJSE.injectGlobalJSFunction(DoricConstant.INJECT_TIMER_CLEAR, new JavaFunction() {
            @Override
            public JavaValue exec(JSDecoder[] args) {
                try {
                    mTimerExtension.clearTimer(args[0].number().longValue());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
        mDoricJSE.injectGlobalJSFunction(DoricConstant.INJECT_BRIDGE, new JavaFunction() {
            @Override
            public JavaValue exec(JSDecoder[] args) {
                try {
                    String contextId = args[0].string();
                    String module = args[1].string();
                    String method = args[2].string();
                    String callbackId = args[3].string();
                    JSDecoder jsDecoder = args[4];
                    return mDoricBridgeExtension.callNative(contextId, module, method, callbackId, jsDecoder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    private void initHugoRuntime() {
        loadBuiltinJS(DoricConstant.DORIC_BUNDLE_SANDBOX);
        String libName = DoricConstant.DORIC_MODULE_LIB;
        String libJS = DoricUtils.readAssetFile(DoricConstant.DORIC_BUNDLE_LIB);
        mDoricJSE.loadJS(packageModuleScript(libName, libJS), "Module://" + libName);
    }

    @Override
    public boolean handleMessage(Message msg) {
        return false;
    }

    public void teardown() {
        mDoricJSE.teardown();
    }

    private void loadBuiltinJS(String assetName) {
        String script = DoricUtils.readAssetFile(assetName);
        mDoricJSE.loadJS(script, "Assets://" + assetName);
    }

    public void prepareContext(final String contextId, final String script, final String source) {
        mDoricJSE.loadJS(packageContextScript(contextId, script), "Context://" + source);
    }

    public void destroyContext(final String contextId) {
        mDoricJSE.loadJS(String.format(DoricConstant.TEMPLATE_CONTEXT_DESTROY, contextId), "_Context://" + contextId);
    }

    private String packageContextScript(String contextId, String content) {
        return String.format(DoricConstant.TEMPLATE_CONTEXT_CREATE, content, contextId, contextId, contextId);
    }

    private String packageModuleScript(String moduleName, String content) {
        return String.format(DoricConstant.TEMPLATE_MODULE, moduleName, content);
    }

    public JSDecoder invokeDoricMethod(final String method, final Object... args) {
        ArrayList<JavaValue> values = new ArrayList<>();
        for (Object arg : args) {
            values.add(DoricUtils.toJavaValue(arg));
        }
        return mDoricJSE.invokeMethod(DoricConstant.GLOBAL_DORIC, method,
                values.toArray(new JavaValue[values.size()]), true);
    }

    @Override
    public void callback(long timerId) {
        try {
            invokeDoricMethod(DoricConstant.DORIC_TIMER_CALLBACK, timerId);
        } catch (Exception e) {
            e.printStackTrace();
            DoricLog.e("Timer Callback error:%s", e.getLocalizedMessage());
        }
    }

    public DoricRegistry getRegistry() {
        return mDoricRegistry;
    }
}
