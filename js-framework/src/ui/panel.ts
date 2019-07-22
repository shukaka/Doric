import './../runtime/global'
import { View, Stack, Group } from "./view";
import { loge, log } from '../util/log';
import { Model } from '../util/types';
import { Context } from '../runtime/sandbox';


export function NativeCall(target: Panel, propertyKey: string, descriptor: PropertyDescriptor) {
    const originVal = descriptor.value
    descriptor.value = function () {
        const ret = Reflect.apply(originVal, this, arguments)
        return ret
    }
    return descriptor
}

type Frame = { width: number, height: number }

export abstract class Panel {
    context?: Context
    onCreate() { }
    onDestory() { }
    onShow() { }
    onHidden() { }

    abstract build(rootView: Group): void

    private __data__: any
    private __rootView__: Group = new Stack

    getRootView() {
        return this.__rootView__
    }
    getInitData() {
        return this.__data__
    }

    @NativeCall
    private __init__(frame: Frame, data: any) {
        log('__init__:', frame, data)
        this.__data__ = data
        this.__rootView__.width = frame.width
        this.__rootView__.height = frame.height
        this.build(this.__rootView__)
    }

    @NativeCall
    private __onCreate__() {
        this.onCreate()
    }

    @NativeCall
    private __onDestory__() {
        this.onDestory()
    }

    @NativeCall
    private __onShow__() {
        this.onShow()
    }

    @NativeCall
    private __onHidden__(): void {
        this.onHidden()
    }

    @NativeCall
    private __build__() {
        this.build(this.__rootView__)
    }

    @NativeCall
    private __response__(viewIds: string[], callbackId: string) {
        const v = this.retrospectView(viewIds)
        if (v === undefined) {
            loge(`Cannot find view for ${viewIds}`)
        }
        const argumentsList: any = [callbackId]
        for (let i = 2; i < arguments.length; i++) {
            argumentsList.push(arguments[i])
        }
        Reflect.apply(v.responseCallback, v, argumentsList)
    }

    private retrospectView(ids: string[]): View {
        return ids.reduce((acc: View, cur) => {
            if (acc instanceof Group) {
                return acc.children.filter(e => e.viewId === cur)[0]
            }
            return acc
        }, this.__rootView__)
    }

    private nativeRender(model: Model) {
        if (this.context) {
            this.context.bridge.shader_render(model)
        }
    }

    private hookBeforeNativeCall() {
    }

    private hookAfterNativeCall() {
        if (this.__rootView__.isDirty()) {
            const model = this.__rootView__.toModel()
            this.nativeRender(model)
            this.__rootView__.clean()
        }
    }

}