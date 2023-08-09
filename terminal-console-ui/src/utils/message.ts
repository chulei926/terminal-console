import {markRaw} from 'vue'
import {ElMessageBox} from 'element-plus'
import {Failed} from '@element-plus/icons-vue'

export function showError(msg: string) {
    return ElMessageBox.alert(msg, '错误', {
        type: 'error',
        icon: markRaw(Failed),
    })
}