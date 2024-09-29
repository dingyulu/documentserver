var Editor = function () {
    var docEditor;
    var сonnectEditor = function (document, editorConfig) {

        var innerAlert = function (message, inEditor) {
            if (console && console.log) {
                console.log(message);
            }
            if (inEditor && docEditor) {
                docEditor.showMessage(message);
            }
        };

        if (!document) {
            innerAlert("文档未指定！");
            return null;
        }

        var config = {
            document: document,
            width: '100%',
            height: '100%',
            documentType: '',
            editorConfig: {
                callbackUrl: editorConfig.callbackUrl,
                canUseHistory: true,
                user: editorConfig.user,
                lang: 'zh-CN',
                //mode: 'view',
                //mode: 'edit',
                mode:editorConfig.mode ==null? "edit":editorConfig.mode,
                recent: [],
                // 自定义一些配置
                //customer: {
                customization: {
                    // 禁用聊天菜单按钮
                    chat: false,
                    // 仅能编辑和删除其注释
                    commentAuthorOnly: false,
                    // 隐藏文档注释菜单按钮
                    comments: false,
                    // 隐藏附加操作按钮
                    compactHeader: false,
                    // 完整工具栏(true代表紧凑工具栏)
                    compactToolbar: false,
                    feedback: {
                        // 隐藏反馈按钮
                        visible: true
                    },
                    // true 表示强制文件保存请求添加到回调处理程序
                    forcesave: false,
                    goback: false,
                    /*{
                        "blank": true, // 转到文档时，在新窗口打开网站(false表示当前窗口打开)
                        "text": "转到文档位置（可以考虑放文档打开源页面）",
                        // 文档打开失败时的跳转也是该地址
                        "url": "http://www.lezhixing.com.cn"
                    },*/
                    // 隐藏帮助按钮
                    help: true,
                    // 首次加载时隐藏右侧菜单(true 为显示)
                    hideRightMenu: false,
                    // 加载编辑器时自动显示/隐藏审阅更改面板(true显示 false隐藏)
                    showReviewChanges: false,
                    // 清楚地显示顶部工具栏选项卡(true 代表仅突出显示以查看选择了哪一个)
                    toolbarNoTabs: false,
                    // 定义文档显示缩放百分比值
                    //zoom: -2,
                   // zoom: 100,
                    zoom: editorConfig.zoom,

                    defalut: editorConfig.defalut,
                    mobileForceView: false
                }
            },
            events: {
                // 应用程序被加载到浏览器中
                onAppReady: function() {
                    console.log('Document Editor is ready');
                },
                // 当文档被其他用户在 严格 的共同编辑模式下共同编辑时调用的函数
                onCollaborativeChanges: function() {
                    console.log('The document changed by collaborative user');
                },
                // 当文档加载到文档编辑器时调用的函数
                onDocumentReady: function() {
                    console.log('Document is loaded..2...');
                    var contentControls = [];
                    window.connector= docEditor.createConnector();
                    console.log('contentControls. data..22..',window.connector);
                    connector.executeMethod("GetAllContentControls", null, function (data) {
                        console.log('contentControls. data....',data);
                        setTimeout(function () {
                            for (let i = 0; i < data.length; i++) {

                            //...

                                connector.executeMethod("GetFormValue", [data[i]["InternalId"]], function (value) {
                                    data[i].Value = value ? value : "";
                                    if (data.length - 1 == i) {
                                        contentControls = data;

                                        console.log('contentControls.....',data);
                                  //  ...
                                    }
                                });
                            }
                        }, 0);
                    });
                },
                // 修改文档时调用的函数。 使用以下参数调用它：{"data": true} --适用于当前用户正在编辑文档时。使用以下参数调用它：{"data": false} --适用于当前用户的更改发送到 文档编辑服务时
                onDocumentStateChange: function (event) {
                    var title = document.title.replace(/\*$/g, '');
                    if (event.data) {
                        console.log('The document changed');
                        document.title = title + '*';
                    } else {
                        console.log("Changes are collected on document editing service");
                    }
                },
                // 调用 downloadAs 方法时使用已编辑文件的绝对 URL 调用的函数。 要下载的文档的绝对 URL 及其类型在 data 参数中发送。
                onDownloadAs: function (event) {
                    var fileType = event.data.fileType;
                    var url = event.data.url;
                    console.log("TENCODING Document Editor create file: " + url);
                },
                // 发生错误或其他特定事件时调用的函数。 错误消息在 data 参数中发送
                onError: function (event) {
                    console.log("TENCODING Document Editor reports an error: code " + event.data.errorCode + ", description " + event.data.errorDescription);
                },
                // 应用程序打开文件时调用的函数。 模式在 data.mode 参数中发送。 可以 查看 或 编辑。
                onInfo: function (event) {
                    console.log("TENCODING Document Editor is opened in mode " + event.data.mode);
                },
                /**
                 * 通过 meta 命令更改文档的元信息时调用的函数。 文档的名称在 data.title 参数中发送。
                 * 收藏 图标高亮状态在 data.favorite 参数中发送。 当用户点击 收藏 图标时，
                 * 调用setFavorite方法更新 收藏 图标高亮状态信息。 如果未声明该方法，则收藏 图标不会更改。
                 * @param event
                 */
                onMetaChange: function (event) {
                    if (event.data.favorite) {
                        var favorite = !!event.data.favorite;
                        var title = document.title.replace(/^\☆/g, "");
                        document.title = (favorite ? "☆" : "") + title;
                        docEditor.setFavorite(favorite);
                    }
                    innerAlert("onMetaChange: " + JSON.stringify(event.data));
                },
                /**
                 * 当用户试图获取打开包含书签的文档的链接时调用的函数，滚动到书签位置。
                 * 要设置书签链接，您必须调用 setActionLink 方法。 书签数据在 data 参数中接收，
                 * 然后必须在配置中用作 editorConfig.actionLink 参数的值。 如果未声明该方法，则不会显示 获取链接 按钮。
                 * @param event
                 */
                onMakeActionLink: function (event){
                    var actionData = event.data;
                    var linkParam = JSON.stringify(actionData);
                    docEditor.setActionLink(replaceActionLink(location.href, linkParam));
                },
                /**
                 * 显示 错误 后调用的函数，当使用旧的 document.key 值打开文档进行编辑时，
                 * 该值用于编辑以前的文档版本并成功保存。 调用此事件时，必须使用新的 document.key重新初始化编辑器。
                 */
                onOutdatedVersion: function () {
                    location.reload(true);
                },
                // 当所有插件都加载并可以使用时调用的函数。
                onPluginsReady: function () {
                },
                // 当编辑器的工作必须结束并且编辑器必须关闭时调用的函数。
                onRequestClose: function () {
                    if (window.opener) {
                        window.close();
                        return;
                    }
                    docEditor.destroyEditor();
                },
                // 当用户试图通过单击 编辑文档 按钮将文档从查看模式切换到编辑模式时调用的函数。 调用该函数时，编辑器必须再次初始化，处于编辑模式。 如果未声明该方法，则不会显示 编辑 按钮。
                onRequestEditRights: function () {
                    console.log("TENCODING Document Editor requests editing rights");
                    config.mode = 'view';
                    document.location.reload();
                    // var he=location.href.replace("view","edit");
                    // location.href=he;
                },
                /**
                 * 当用户试图通过单击 版本历史 按钮来显示文档版本历史时调用的函数。
                 * 要显示文档版本历史记录，您必须调用 refreshHistory 方法。
                 * 如果未声明该方法和 onRequestHistoryData 方法，则不会显示 版本历史 按钮。
                 * 其中 changes 是保存文档后返回的 历史对象 的 更改。
                 * 其中 serverVersion 是保存文档后返回的 历史对象 中的 serverVersion 。
                 */
                onRequestHistory : function () {
                    let fileId = (editorConfig.callbackUrl).match(/id=(\d+)(?:&|$)/)[1];
                    // console.log(fileId,"1111111111111111111111111111");

                    $.ajax({
                        url: "https://doc.baizhanke.com/api/history?fileId="+fileId,
                        success: function (result) {
                            // console.log("成功:" + JSON.stringify(result));
                            // console.log(result.length);
                            docEditor.refreshHistory({
                                    "currentVersion": result.length,
                                    "history": result,
                            });
                        },
                        error: function (result) {
                            console.log("错误:" + JSON.stringify(result));
                        },
                    });
                    // docEditor.refreshHistory({
                    //     "currentVersion": 2,
                    //     "history": [
                    //         {
                    //             "changes": changes,
                    //             "created": "2010-07-06 10:13 AM",
                    //             "key": "af86C7e71Ca8",
                    //             "serverVersion": serverVersion,
                    //             "user": {
                    //                 "id": "F89d8069ba2b",
                    //                 "name": "Kate Cage"
                    //             },
                    //             "version": 1
                    //         },
                    //         {
                    //             "changes": changes,
                    //             "created": "2010-07-07 3:46 PM",
                    //             "key": "Khirz6zTPdfd7",
                    //             "user": {
                    //                 "id": "78e1e841",
                    //                 "name": "John Smith"
                    //             },
                    //             "version": 2
                    //         }
                    //     ]
                    // });
                },
                /**
                 * 当用户试图通过单击 关闭历史记录 按钮查看文档版本历史记录返回文档时调用的函数。
                 * 调用该函数时，编辑器必须再次初始化，处于编辑模式。
                 * 如果未声明该方法，则不会显示 关闭历史记录 按钮。
                 */
                onRequestHistoryClose: function () {
                    // console.log("关闭历史记录！！！！");
                    // console.log(window.document);
                    window.document.location.reload();
                    // console.log(window.document,"关闭历史按钮document........");
                },
                /**
                 * 当用户试图点击文档版本历史中的特定文档版本时调用的函数。
                 * 要显示与特定文档版本相对应的更改，您必须调用 setHistoryData 方法。
                 * 文档版本号在 data 参数中发送。 如果未声明该方法和 onRequestHistory 方法，则不会显示 版本历史 按钮。
                 * @param event
                 */
                onRequestHistoryData: async function (event) {
                    let fileId2 = (editorConfig.callbackUrl).match(/id=(\d+)(?:&|$)/)[1];
                    var version = event.data;
                    // console.log("onRequestHistoryData-----event",event);
                    var version2 = version - 1;
                    var shouldShowPrevious = false;
                    if(version2 != 0){
                        shouldShowPrevious = true;
                        response1 = await $.ajax({
                            url: "https://doc.baizhanke.com/api/changes?version="+version2+"&fileId2="+fileId2,
                        })
                    }
                     $.ajax({
                        url: "https://doc.baizhanke.com/api/changes?version="+version+"&fileId2="+fileId2,
                        success: function (result) {
                            // console.log("成功:" + JSON.stringify(result));
                            var url = result.url;
                            var key = result.key;
                            var version = result.version;
                            var changesurl = result.changesurl;
                            var fileType = result.fileType;
                            docEditor.setHistoryData({
                                "changesurl": changesurl,
                                "fileType": fileType,
                                "key": key,
                                "previous": shouldShowPrevious ? {
                                    "fileType": response1.fileType,
                                    "key": response1.key,
                                    "url": response1.url
                                } : {},
                                "url": url,
                                "version": version,
                            });
                        },
                        error: function (result) {
                            // console.log("错误:" + JSON.stringify(result));
                        },
                    });
                    // docEditor.setHistoryData({
                    //     "changesUrl": "https://example.com/url-to-changes.zip",
                    //     "fileType": "docx",
                    //     "key": "Khirz6zTPdfd7",
                    //     "previous": {
                    //         "fileType": "docx",
                    //         "key": "af86C7e71Ca8",
                    //         "url": "https://example.com/url-to-the-previous-version-of-the-document.docx"
                    //     },
                    //     "url": "https://example.com/url-to-example-document.docx",
                    //     "version": version
                    // })
                },
                /**
                 * 当用户尝试通过单击 存储中的图像 按钮插入图像时调用的函数。
                 * 图像插入的类型在参数 data.c中指定。 要将图像插入文件，
                 * 您必须使用指定的命令调用 insertImage 方法。 如果未声明该方法，
                 * 则不会显示 Image from Storage 按钮。
                 * @param event
                 */
                onRequestInsertImage: function (event) {
                    // docEditor.insertImage({
                    //     c: event.data.c,
                    //     images: [
                    //         {
                    //             "fileType": "png",
                    //             "url": "https://example.com/url-to-example-image1.png"
                    //         },
                    //         {
                    //             "fileType": "png",
                    //             "url": "https://example.com/url-to-example-image2.png"
                    //         }
                    //     ]
                    //  });
                },
                // 当用户试图通过单击 重命名... 按钮重命名文件时调用的函数
                onRequestRename: function (event) {
                    var title = event.data;
                },
                /**
                 * 当用户试图通过单击版本历史记录中的 恢复 按钮来恢复文件版本时调用的函数。
                 * 调用该函数时，必须再次调用 refreshHistory 方法来初始化版本历史。
                 * 如果从历史记录中调用文档版本，则在 data.version 参数中发送文档版本号。
                 * 此外，如果从 历史对象中调用文档更改，则会在 data.url 参数中发送文档链接。
                 * 使用此链接指定的文档类型在 data.fileType 中发送范围。 如果未声明该方法，则不会显示 恢复 按钮。
                 * 其中 changes 是保存文档后返回的 历史对象 的 更改。
                 * 其中 serverVersion 是保存文档后返回的 历史对象 中的 serverVersion。
                 * @param event
                 */
                // onRequestRestore:  function (event) {
                //     try{
                //         console.log(event,"点击恢复按钮，event事件........");
                //         var fileType = event.data.fileType;
                //         var url = event.data.url;
                //         var version = event.data.version;
                //         let fileId3 = (editorConfig.callbackUrl).match(/id=(\d+)(?:&|$)/)[1];
                //         //点击恢复查询要恢复的这条记录，版本更该为最新版本添加到历史数据中
                //         let response = $.ajax({
                //             url: "https://106.54.209.197:8099/api/oneHistory?version="+version+"&fileId="+fileId3,
                //         });
                //         console.log(response,"1111111aaaaaaaaaaaa")
                //         //版本号
                //         let response2 = $.ajax({
                //             url: "https://106.54.209.197:8099/api/history?fileId="+fileId3,
                //         });
                //         console.log(response2,"222222222aaaaaaaaaaaa")
                //         $.when(response, response2).then(function (response, response2) {
                //             var responseElement = response[0];
                //             var response2Element = response2[0];
                //             console.log(response[0],"11111111111111.......................")
                //             console.log(response2,"11111111111111.......................")
                //             $.ajax({
                //                 url: "https://106.54.209.197:8099/api/addHistory",
                //                 type: "POST",
                //                 contentType: "application/json; charset=utf-8",
                //                 data:JSON.stringify ({
                //                     "serverVersion": responseElement.serverVersion,
                //                     "created": responseElement.created,
                //                     "userId": responseElement.userId,
                //                     "userName": responseElement.userName,
                //                     "docKey": responseElement.docKey,
                //                     "version":response2Element.length + 1,
                //                     "url": responseElement.url,
                //                     "changesUrl": responseElement.changesUrl,
                //                     "fileId": responseElement.fileId,
                //                     "fileType": fileType
                //                 }),
                //                 success: function (result) {
                //                     $.ajax({
                //                         url: "https://106.54.209.197:8099/api/history?fileId="+fileId3,
                //                         success: function (result) {
                //                             console.log("成功:" + JSON.stringify(result));
                //                             console.log(result.length);
                //                             docEditor.refreshHistory({
                //                                 "currentVersion": result.length,
                //                                 "history": result,
                //                             });
                //                         },
                //                         error: function (result) {
                //                             console.log("错误:" + JSON.stringify(result));
                //                         },
                //                     });
                //                     console.log("成功:" + JSON.stringify(result));
                //                 },
                //                 error: function (result) {
                //                     console.log("错误:" + JSON.stringify(result));
                //                 },
                //             });
                //         });
                //     }catch (e) {
                //         console.log(e.message);
                //     }
                //
                //
                //     // var fileType = event.data.fileType;
                //     // var url = event.data.url;
                //     // var version = event.data.version;
                //     // docEditor.refreshHistory({
                //     //     "currentVersion": 2,
                //     //     "history": [
                //     //         {
                //     //             "changes": changes,
                //     //             "created": "2010-07-06 10:13 AM",
                //     //             "key": "af86C7e71Ca8",
                //     //             "serverVersion": serverVersion,
                //     //             "user": {
                //     //                 "id": "F89d8069ba2b",
                //     //                 "name": "Kate Cage"
                //     //             },
                //     //             "version": 1
                //     //         },
                //     //         {
                //     //             "changes": changes,
                //     //             "created": "2010-07-07 3:46 PM",
                //     //             "key": "Khirz6zTPdfd7",
                //     //             "user": {
                //     //                 "id": "78e1e841",
                //     //                 "name": "John Smith"
                //     //             },
                //     //             "version": 2
                //     //         },
                //     //     ]
                //     // });
                // }

            }
        };

        docEditor = new DocsAPI.DocEditor("iframeEditor", config);
    };

    return {
        init: function (document, editorConfig) {
            сonnectEditor(document, editorConfig);
        }
    }
}();
