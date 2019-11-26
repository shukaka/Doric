import { View, Property, Superview, IView } from "./view";
import { List } from "./list";
import { Scroller } from "./scroller";
import { BridgeContext } from "../runtime/global";
import { layoutConfig } from "./declarative";

export interface IRefreshable extends IView {
    content: List | Scroller
    header?: View
    onRefresh?: () => void
}

export class Refreshable extends Superview implements IRefreshable {

    content!: List | Scroller

    header?: View

    @Property
    onRefresh?: () => void

    allSubviews() {
        const ret: View[] = [this.content]
        if (this.header) {
            ret.push(this.header)
        }
        return ret
    }

    setRefreshable(context: BridgeContext, refreshable: boolean) {
        return this.nativeChannel(context, 'setRefreshable')(refreshable)
    }

    setRefreshing(context: BridgeContext, refreshing: boolean) {
        return this.nativeChannel(context, 'setRefreshing')(refreshing)
    }

    isRefreshable(context: BridgeContext) {
        return this.nativeChannel(context, 'isRefreshable')() as Promise<boolean>
    }

    isRefreshing(context: BridgeContext) {
        return this.nativeChannel(context, 'isRefreshing')() as Promise<boolean>
    }

    toModel() {
        this.dirtyProps.content = this.content.viewId
        this.dirtyProps.header = (this.header || {}).viewId
        return super.toModel()
    }
}

export function refreshable(config: IRefreshable) {
    const ret = new Refreshable
    ret.layoutConfig = layoutConfig().wrap()
    for (let key in config) {
        Reflect.set(ret, key, Reflect.get(config, key, config), ret)
    }
    return ret
}
