<template>
	<el-tabs v-model="editableTabsValue" type="card" :style="{width: whStyle.width + 'px', height: whStyle.height + 'px'}" editable @tab-remove="removeTab" @tab-add="addTab()">
		<el-tab-pane v-for="item in editableTabs" :key="item.name" :name="item.name" :lazy="true">
			<template #label>
		        <span>
		          <span>{{ item.title }}</span>
		        </span>
			</template>
			<component :is="item.com" :sid="item.sid"></component>
		</el-tab-pane>
	</el-tabs>

	<el-dialog v-model="dialogFormVisible" title="连接属性" width="520" draggable :close-on-click-modal="false">
		<el-form :model="form">
			<el-form-item label="协议" :label-width="formLabelWidth">
				<el-col :span="12">
					<el-radio-group v-model="form.protocol">
						<el-radio label="SSH" size="large">SSH</el-radio>
						<el-radio label="TELNET" size="large">TELNET</el-radio>
					</el-radio-group>
				</el-col>
			</el-form-item>
			<el-form-item label="厂家" :label-width="formLabelWidth">
				<el-col :span="12">
					<el-radio-group v-model="form.vendor">
						<el-radio label="LINUX" size="large">LINUX</el-radio>
						<el-radio label="HW" size="large">HW</el-radio>
					</el-radio-group>
				</el-col>
			</el-form-item>

			<el-form-item label="主机" :label-width="formLabelWidth">
				<el-col :span="11">
					<el-input v-model="form.ip" placeholder="请输入IP地址" autocomplete="off"/>
				</el-col>
				<el-col :span="1">
					<el-input-number v-model="form.port" :min="1" :max="65535" controls-position="right" placeholder="请输入端口号"/>
				</el-col>
			</el-form-item>
			<el-form-item label="用户名" :label-width="formLabelWidth">
				<el-col :span="20">
					<el-input v-model="form.username" autocomplete="off" placeholder="请输入用户名"/>
				</el-col>
			</el-form-item>
			<el-form-item label="密码" :label-width="formLabelWidth">
				<el-col :span="20">
					<el-input v-model="form.pwd" type="password" show-password autocomplete="off" placeholder="请输入密码"/>
				</el-col>
			</el-form-item>
		</el-form>
		<template #footer>
      <span class="dialog-footer">
        <el-button @click="sshConnCancel">取消</el-button>
        <el-button type="primary" @click="sshConnConfirm">
          连接
        </el-button>
      </span>
		</template>
	</el-dialog>
</template>

<script lang="ts" setup>
import {defineAsyncComponent, reactive, ref, shallowRef} from 'vue'
import {ElMessage} from 'element-plus'
import {auth} from './api/ssh'

const dialogFormVisible = ref(false)
const formLabelWidth = '80px'
const form = reactive({
	ip: '192.168.60.60',
	port: 9999,
	username: 'huawei_user',
	pwd: 'Huawei@1234!',
	protocol: 'TELNET',
	vendor: 'HW',
})

let tabIndex = 0
const editableTabsValue = ref('')
const editableTabs = shallowRef(new Array())
const whStyle = ref({
	height: 50,
	width: document.body.clientWidth
})


const addTab = () => {
	// TODO 打开对话框，创建连接
	dialogFormVisible.value = true;
}
const removeTab = (targetName: string) => {
	// TODO 断开连接
	const tabs = editableTabs.value
	let activeName = editableTabsValue.value
	if (activeName === targetName) {
		tabs.forEach((tab, index) => {
			if (tab.name === targetName) {
				tab.sid = null;
				const nextTab = tabs[index + 1] || tabs[index - 1]
				if (nextTab) {
					activeName = nextTab.name
				}
			}
		})
	}
	editableTabsValue.value = activeName
	editableTabs.value = tabs.filter((tab) => tab.name !== targetName)
	console.log('移除tab:', editableTabs.value)
	for (let i = 0; i < editableTabs.value.length; i++) {
		if (editableTabs.value[i].name == targetName) {
			editableTabs.value[i].sid = null;
		}
	}
}

const sshConnConfirm = () => {
	auth(form).then((res: any) => {
		console.log('认证结果：', res.sid)
		const newTabName = `SSH${++tabIndex}`
		editableTabs.value.push({
			title: newTabName,
			name: newTabName,
			sid: res.sid,
			com: defineAsyncComponent(() => import("./views/WebShell.vue"))
		})
		editableTabsValue.value = newTabName
		dialogFormVisible.value = false;
	}).catch((err: any) => {
		console.error('认证失败', err)
		dialogFormVisible.value = false;
		ElMessage.error(err)
	})
}

const sshConnCancel = () => {
	dialogFormVisible.value = false;
}
</script>

<style lang="scss" scoped>
//.demo-tabs{
//	width: 1000px;
//}
</style>
