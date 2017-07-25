# im
拉取聊天消息http://47.92.98.23:8080/message/a?roomId=8&from=1
。第一次进入房间后，from为0，接着定时拉取，from的值为上一次response的time+1
