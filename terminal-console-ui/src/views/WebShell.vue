<template>
	<div class="term-box" :style="{width: boxStyle.width + 'px', height: boxStyle.height + 'px'}">
		<div :id="boxId"></div>
	</div>
</template>

<script lang="ts" setup>
import {nextTick, ref, toRefs} from 'vue'
import {Terminal} from 'xterm'
import {FitAddon} from 'xterm-addon-fit'
import "xterm/css/xterm.css";

import {v4 as uuidv4} from 'uuid';

import {ElLoading} from 'element-plus'
import {AdventureTime} from 'xterm-theme';

const props = defineProps({
	sid: {
		type: String,
		default: NaN
	}
})

const loadingInstance = ElLoading.service({lock: true, text: '连接中...'})
const boxId = ref(uuidv4())

const boxStyle = ref({
	height: document.documentElement.clientHeight || document.body.clientHeight - 50,
	width: document.documentElement.clientWidth || document.body.clientWidth
})


let term: any = ref(null)
let fitAddon: any = ref(null)
const termHeight = ref(Number.parseInt(`${boxStyle.value.height / 18}`));


const cmd: any = ref('');
let ws: any = null;
let wsHeartChecker: any = {};


const initTerm = () => {
	const {sid} = toRefs(props)
	let termContainer: any = document.getElementById(boxId.value)
	term = new Terminal({
		cursorBlink: true,
		cols: 100,
		rows: termHeight.value,
		lineHeight: 1,
		disableStdin: false,
		theme: AdventureTime
	})
	term.open(termContainer)
	connWs();
	setTimeout(() => {
		ws.send(JSON.stringify({sid: sid?.value, command: ''}));
	}, 1000)
	// 换行并输入起始符 $
	// term.prompt = () => {
	// 	term.write("\r\n\x1b[33m$\x1b[0m ")
	// }
	// 添加事件监听器，支持输入方法
	term.onKey((e: any) => {
		const printable = !e.domEvent.altKey && !e.domEvent.altGraphKey && !e.domEvent.ctrlKey && !e.domEvent.metaKey
		if (e.domEvent.keyCode === 13) {
			ws.send(JSON.stringify({sid: sid?.value, command: `${cmd.value}\n`}));
			cmd.value = '';
		} else if (e.domEvent.keyCode === 8) { // back 删除的情况
			if (term._core.buffer.x > 2) {
				term.write('\b \b')
				cmd.value = cmd.value.substring(0, cmd.value.length - 2)
				console.log(cmd.value)
			}
		} else if (printable) {
			term.write(e.key)
			cmd.value = `${cmd.value}${e.key}`
		}
		// console.log(1, 'print', e.key)
	})
	term.onData((key: any) => {  // 粘贴的情况
		if (key.length > 1) term.write(key)
	})
	// canvas背景全屏
	fitAddon = new FitAddon()
	term.loadAddon(fitAddon)
	fitAddon.fit()
}

const connWs = () => {
	const {sid} = toRefs(props)
	console.log('子组件获取到 sid: ', sid)
	const url = `ws://localhost:50000/app`;
	ws = new WebSocket(url);
	wsHeartChecker = {
		timeout: 60000,
		timeoutObj: null,
		reset: function () {
			clearInterval(this.timeoutObj);
			this.start();
		},
		start: function () {
			this.timeoutObj = setInterval(function () {
				if (ws.readyState === 1) {
					ws.send("hb");
				}
			}, this.timeout)
		}
	};
	ws.onopen = wsOnopen;
	ws.onmessage = wsOnmessage;
	ws.onclose = wsOnclose;
}

const wsOnopen = (event: any) => {
	console.log("onopen: ", event);
	wsHeartChecker.start();
}

const wsOnmessage = (message: any) => {
	// 无论收到什么信息，说明当前连接正常，将心跳检测的计时器重置
	wsHeartChecker.reset();
	console.log("client received a message.data: " + message.data);
	if (message.data !== "hb_ok") {
		// 不要将ping的答复信息输出
		term.write(message.data)
	}
}

const wsOnclose = (event: any) => {
	if (event.code !== 4500) {
		//4500为服务端在打开多tab时主动关闭返回的编码
		// connect();
	}
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