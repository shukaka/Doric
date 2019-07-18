const path = require('path')
const fs = require('fs')
import resolve from 'rollup-plugin-node-resolve'
import commonjs from 'rollup-plugin-commonjs'

export default [
    {
        input: "build/index.runtime.js",
        output: {
            name: "hego",
            format: "iife",
            file: "bundle/hego-sandbox.js",
        },
        sourceMap: true,
        plugins: [
            resolve({ jsnext: true, main: true }),
            commonjs()
        ]
    },
    {
        input: "build/index.js",
        output: {
            format: "cjs",
            file: "bundle/hego-lib.js",
        },
        sourceMap: true,
        plugins: [
            resolve({ jsnext: true, main: true }),
        ]
    },
    {
        input: "build/demo.js",
        output: {
            format: "cjs",
            file: "demo/demo.js",
        },
        sourceMap: true,
        plugins: [
            resolve({ jsnext: true, main: true }),
            commonjs()
        ],
        external: ['./index'],
    },
]