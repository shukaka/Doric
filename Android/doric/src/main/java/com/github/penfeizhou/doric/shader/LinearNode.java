package com.github.penfeizhou.doric.shader;

import android.graphics.drawable.ShapeDrawable;
import android.widget.LinearLayout;

import com.github.penfeizhou.doric.DoricContext;
import com.github.penfeizhou.doric.utils.DoricUtils;
import com.github.pengfeizhou.jscore.JSObject;
import com.github.pengfeizhou.jscore.JSValue;

/**
 * @Description: com.github.penfeizhou.doric.shader
 * @Author: pengfei.zhou
 * @CreateDate: 2019-07-23
 */
public class LinearNode extends GroupNode<LinearLayout> {
    public LinearNode(DoricContext doricContext) {
        super(doricContext);
    }

    @Override
    public LinearLayout build(JSObject jsObject) {
        return new LinearLayout(getContext());
    }

    @Override
    protected void blend(LinearLayout view, String name, JSValue prop) {
        switch (name) {
            case "space":
                ShapeDrawable shapeDrawable;
                if (view.getDividerDrawable() == null) {
                    shapeDrawable = new ShapeDrawable();
                    shapeDrawable.setAlpha(0);
                    view.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                } else {
                    shapeDrawable = (ShapeDrawable) view.getDividerDrawable();
                    view.setDividerDrawable(null);
                }
                if (view.getOrientation() == LinearLayout.VERTICAL) {
                    shapeDrawable.setIntrinsicHeight(DoricUtils.dp2px(prop.asNumber().toFloat()));
                } else {
                    shapeDrawable.setIntrinsicWidth(DoricUtils.dp2px(prop.asNumber().toFloat()));
                }
                view.setDividerDrawable(shapeDrawable);
                break;
            case "gravity":
                view.setGravity(prop.asNumber().toInt());
                break;
            default:
                super.blend(view, name, prop);
                break;
        }
    }
}