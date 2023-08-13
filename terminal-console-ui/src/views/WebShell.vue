<template>
	<div class="term-box" :style="{width: boxStyle.width + 'px', height: boxStyle.height + 'px'}">
		<div :id="boxId"></div>
	</div>
</template>

<script lang="ts" setup>
import {nextTick, ref, toRefs, watch} from 'vue'
import {Terminal} from 'xterm'
import {FitAddon} from 'xterm-addon-fit'
import {AttachAddon} from 'xterm-addon-attach';
import "xterm/css/xterm.css";

import {v4 as uuidv4} from 'uuid';

import {ElLoading} from 'element-plus'
import {common as xtermTheme} from 'xterm-style'

const props = defineProps({
	sid: {
		type: Object,
		default: NaN
	}
})

watch(props.sid, () => {
	console.log('监听到sid发生变化:', props.sid)
})

const loadingInstance = ElLoading.service({lock: true, text: '连接中...'})
const boxId = ref(uuidv4())

const boxStyle = ref({
	height: document.documentElement.clientHeight || document.body.clientHeight - 40,
	width: document.body.clientWidth
})

let term: any = ref(null)
let fitAddon: any = ref(null)
const termHeight = ref(Number.parseInt(`${boxStyle.value.height / 18}`));

let ws: any = null;
// let wsHeartChecker: any = {};

const initTerm = () => {
	loadingInstance.visible.value = true;
	connWs();

	let termContainer: any = document.getElementById(boxId.value)
	term = new Terminal({
		cols: 80,
		rows: termHeight.value,
		cursorStyle: 'bar', // 光标样式  null | 'block' | 'underline' | 'bar'
		cursorBlink: true, // 光标闪烁
		convertEol: true, //启用时，光标将设置为下一行的开头
		scrollback: 100, //回滚
		tabStopWidth: 4, //制表宽度
		lineHeight: 1,
		disableStdin: false, //是否应禁用输入。
		theme: xtermTheme
	})

	const attachAddon = new AttachAddon(ws);
	term.loadAddon(attachAddon);

	fitAddon = new FitAddon()
	term.loadAddon(fitAddon)
	term.open(termContainer)
	term.focus();
	fitAddon.fit()
	loadingInstance.visible.value = false;
}

const connWs = () => {
	const {sid} = toRefs(props)
	let sidVal = sid?.value.toString()
	// console.log('子组件获取到 sid: ', sidVal)
	let url = `ws://${window.location.host}${import.meta.env.VITE_WS_BASE_URL}?sid=${sidVal}`;
	ws = new WebSocket(url);
	ws.onopen = wsOnopen;
	ws.onmessage = wsOnmessage;
	ws.onclose = wsOnclose;
}

const wsOnopen = (event: any) => {
	// console.log("onopen: ", event);
	ws.send('\r')
}

const wsOnmessage = (message: any) => {
	// console.log("client received a message.data: " + message.data);
	// term.write(message.data)
}

const wsOnclose = (event: any) => {
	if (event.code !== 4500) {
		//4500为服务端在打开多tab时主动关闭返回的编码
		// connect();

	}
	console.log('服务端关闭')
}

const resizeEvent = () => {
	const h = document.documentElement.clientHeight || document.body.clientHeight
	const w = document.documentElement.clientWidth || document.body.clientWidth
	boxStyle.value = {
		height: h - 50,
		width: w
	}
	fitAddon.fit()
}
window.addEventListener("resize", resizeEvent);


nextTick(() => {
	initTerm();
	loadingInstance.close();
	resizeEvent()

})
</script>