/** !
 * Created by EYHN on 2017/4/17. https://github.com/EYHN
 * 修改自 sexdevil/LSLoader https://github.com/sexdevil/LSLoader
 */

(function () {

    window.lsloader = {
        jsRunSequence: [], //js 运行队列 {name:代码name,code:代码,status:状态 failed/loading/comboJS,path:线上路径}
        jsnamemap: {},     //js name map 防fallback 重复请求资源
        cssnamemap: {}      //css name map 防fallback 重复请求资源
    };

    /*
    * 封装localStorage get set remove 方法
    * try catch保证ls写满或者不支持本地缓存环境能继续运行js
    * */
    lsloader.removeLS = function (key) {
        try {
            localStorage.removeItem(key)
        } catch (e) { }
    };
    lsloader.setLS = function (key, val) {
        try {
            localStorage.setItem(key, val);
        } catch (e) {
        }
    }
    lsloader.getLS = function (key) {
        var val = ''
        try {
            val = localStorage.getItem(key);
        } catch (e) {
            val = '';
        }
        return val
    }

    versionString = "/*" + (window.materialVersion || 'unknownVersion') + "*/";

    lsloader.clean = function () {
        try {
            var keys = [];
            for (var i = 0; i < localStorage.length; i++) {
                keys.push(localStorage.key(i))
            }
            keys.forEach(function (key) {
                var data = lsloader.getLS(key);
                if (window.oldVersion) {
                    var remove = window.oldVersion.reduce(function(p,c) {
                        return p || data.indexOf('/*' + c + '*/') !== -1
                    }, false)
                    if (remove) {
                        lsloader.removeLS(key);
                    }
                }
            })
        } catch (e) {
        }
    }

    lsloader.clean();

    /*
     * load资源
     * name 作为key path/分割符/代码值 作为value ,存储资源
     * 如果value值中取出的版本号和线上模版的一致,命中缓存用本地,
     * 否则 调用requestResource 请求资源
     * jsname 文件name值,取相对路径,对应存在localStroage里的key
     * jspath 文件线上路径,带md5版本号,用于加载资源,区分资源版本
     * cssonload css加载成功时候调用,用于配合页面展现
     * */
    lsloader.load = function (jsname, jspath, cssonload, isJs) {
        if (typeof cssonload === 'boolean') {
            isJs = cssonload;
            cssonload = undefined;
        }
        isJs = isJs || false;
        cssonload = cssonload || function () { };
        var code;
        code = this.getLS(jsname);
        if (code && code.indexOf(versionString) === -1) {   //ls 版本 codestartv* 每次换这个版本 所有ls作废
            this.removeLS(jsname);
            this.requestResource(jsname, jspath, cssonload, isJs);
            return
        }
        //取出对应文件名下的code
        if (code) {
            var versionNumber = code.split(versionString)[0]; //取出路径版本号 如果要加载的和ls里的不同,清理,重写
            if (versionNumber != jspath) {
                console.log("reload:" + jspath)
                this.removeLS(jsname);
                this.requestResource(jsname, jspath, cssonload, isJs);
                return
            }
            code = code.split(versionString)[1];
            if (isJs) {
                this.jsRunSequence.push({ name: jsname, code: code })
                this.runjs(jspath, jsname, code);
            } else {
                document.getElementById(jsname).appendChild(document.createTextNode(code));
                cssonload();
            }
        } else {
            //null xhr获取资源
            this.requestResource(jsname, jspath, cssonload, isJs);
        }
    };


    /*
     * load请求资源
     * 根据文件名尾部不同加载,js走runjs方法,加入运行队列中
     * css 直接加载并且写入对应的<style>标签,根据style的顺序
     * 保证css能正确覆盖规则 css 加载成功后调用cssonload 帮助控制
     * 异步加载样式造车的dom树渲染错乱问题
     * */
    lsloader.requestResource = function (name, path, cssonload, isJs) {
        var that = this
        if (isJs) {
            this.iojs(path, name, function (path, name, code) {
                that.setLS(name, path + versionString + code)
                that.runjs(path, name, code);
            })
        } else {
            this.iocss(path, name, function (code) {
                document.getElementById(name).appendChild(document.createTextNode(code));
                that.setLS(name, path + versionString + code)
            }, cssonload)
        }

    };

    /*
    * iojs
    * 请求js资源,失败后调用jsfallback
    * */
    lsloader.iojs = function (path, jsname, callback) {
        var that = this;
        that.jsRunSequence.push({ name: jsname, code: '' })
        try {
            var xhr = new XMLHttpRequest();
            xhr.open("get", path, true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if ((xhr.status >= 200 && xhr.status < 300) || xhr.status == 304) {
                        if (xhr.response != '') {
                            callback(path, jsname, xhr.response);
                            return;
                        }
                    }
                    that.jsfallback(path, jsname);
                }
            };
            xhr.send(null);
        } catch (e) {
            that.jsfallback(path, jsname);
        }

    };

    /*
     * iocss
     * 请求css资源,失败后调用cssfallback
     * */

    lsloader.iocss = function (path, jsname, callback, cssonload) {
        var that = this;
        try {
            var xhr = new XMLHttpRequest();
            xhr.open("get", path, true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if ((xhr.status >= 200 && xhr.status < 300) || xhr.status == 304) {
                        if (xhr.response != '') {
                            callback(xhr.response);
                            cssonload();
                            return;
                        }
                    }
                    that.cssfallback(path, jsname, cssonload);
                }
            };
            xhr.send(null);

        } catch (e) {
            that.cssfallback(path, jsname, cssonload);
        }
    };

    lsloader.iofonts = function (path, jsname, callback, cssonload) {
        var that = this;
        try {
            var xhr = new XMLHttpRequest();
            xhr.open("get", path, true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if ((xhr.status >= 200 && xhr.status < 300) || xhr.status == 304) {
                        if (xhr.response != '') {
                            callback(xhr.response);
                            cssonload();
                            return;
                        }
                    }
                    that.cssfallback(path, jsname, cssonload);
                }
            };
            xhr.send(null);

        } catch (e) {
            that.cssfallback(path, jsname, cssonload);
        }
    };

    /*
     * runjs
     * 运行js主方法
     * path js线上路径
     * name js相对路径
     * code js代码
     * */

    lsloader.runjs = function (path, name, code) {
        //如果有 name code ,xhr来的结果,写入ls 否则是script.onload调用
        if (!!name && !!code) {
            for (var k in this.jsRunSequence) {
                if (this.jsRunSequence[k].name == name) {
                    this.jsRunSequence[k].code = code;
                }
            }
        }

        if (!!this.jsRunSequence[0] && !!this.jsRunSequence[0].code && this.jsRunSequence[0].status != 'failed') {
            //每次进入runjs检查jsRunSequence,如果第一项有代码并且状态没被置为failed,执行并剔除队列,回调
            var script = document.createElement('script');
            script.appendChild(document.createTextNode(this.jsRunSequence[0].code));
            script.type = 'text/javascript';
            document.getElementsByTagName('head')[0].appendChild(script);
            this.jsRunSequence.shift();
            //如果jsSequence还有排队的 继续运行
            if (this.jsRunSequence.length > 0) {
                this.runjs();
            }

        } else if (!!this.jsRunSequence[0] && this.jsRunSequence[0].status == 'failed') {
            /*每次进入runjs检查jsRunSequence,如果第一项存在并且状态为failed,用script标签异步加载,
             * 并且该项status置为loading 其他资源加载调用runjs时候就不会通过这个js项,等候完成
             */
            var that = this;
            var script = document.createElement('script');
            script.src = this.jsRunSequence[0].path;
            script.type = 'text/javascript';
            this.jsRunSequence[0].status = 'loading'
            script.onload = function () {
                that.jsRunSequence.shift();
                //如果jsSequence还有排队的 继续运行
                if (that.jsRunSequence.length > 0) {
                    that.runjs();
                }
            };
            document.body.appendChild(script);
        }
    }
    /*
    * tagLoad 用script标签加载不支持xhr请求的js资源
    * 方法时jsRunSequence队列中添加一项name path为该资源,但是status=failed的项
    * runjs调用检查时就会把这个项当作失败取用script标签请求
    * */
    lsloader.tagLoad = function (path, name) {
        this.jsRunSequence.push({ name: name, code: '', path: path, status: 'failed' });
        this.runjs();
    }

    //js回退加载 this.jsnamemap[name] 存在 证明已经在队列中 放弃
    lsloader.jsfallback = function (path, name) {
        if (!!this.jsnamemap[name]) {
            return;
        } else {
            this.jsnamemap[name] = name;
        }
        //jsRunSequence队列中 找到fail的文件,标记他,等到runjs循环用script请求
        for (var k in this.jsRunSequence) {
            if (this.jsRunSequence[k].name == name) {
                this.jsRunSequence[k].code = '';
                this.jsRunSequence[k].status = 'failed';
                this.jsRunSequence[k].path = path;
            }
        }
        this.runjs();
    };
    /*cssfallback 回退加载
    * path   同上
    * name   同上
    * cssonload 同上
    * xhr加载css失败的话 使用link标签异步加载样式,成功后调用cssonload
    */
    lsloader.cssfallback = function (path, name, cssonload) {
        if (!!this.cssnamemap[name]) {
            return;
        } else {
            this.cssnamemap[name] = 1;
        }
        var link = document.createElement('link');
        link.type = 'text/css';
        link.href = path;
        link.rel = 'stylesheet';
        link.onload = link.onerror = cssonload;
        var root = document.getElementsByTagName('script')[0];
        root.parentNode.insertBefore(link, root)
    }

    /*runInlineScript 运行行内脚本
   * 如果有依赖之前加载的js的内联脚本,用该方法执行,
   * scriptId js队列中的name值,可选
   * codeId 包含内连脚本的textarea容器的id
   * js队列中添加name code值进入,运行到该项时runjs函数直接把代码append到顶部运行
   */
    lsloader.runInlineScript = function (scriptId, codeId) {
        var code = document.getElementById(codeId).innerText;
        this.jsRunSequence.push({ name: scriptId, code: code })
        this.runjs()
    }
    /*loadCombo combo加载,顺序执行一系列js
     *
     * jslist :[
     * {
     * name:名称,
     * path:线上路径
     * }
     * ]
     * 遍历jslist数组,按照顺序加入jsRunSequence
     * 其中,如果本地缓存成功,直接写入code准备执行
     * 否则status值为comboloading code写入null 不会执行
     * 所有comboloading的模块拼接成一个url请求线上combo服务
     * 成功后执行runcombo方法运行脚本
     * 失败的话所有requestingModules请求的js文件都置为failed
     * runjs会启用script标签加载
     */
    lsloader.loadCombo = function (jslist) {
        var updateList = '';// 待更新combo模块列表
        var requestingModules = {};//存储本次更新map
        for (var k in jslist) {
            var LS = this.getLS(jslist[k].name);
            if (!!LS) {
                var version = LS.split(versionString)[0]
                var code = LS.split(versionString)[1]
            } else {
                var version = '';
            }
            if (version == jslist[k].path) {
                this.jsRunSequence.push({ name: jslist[k].name, code: code, path: jslist[k].path }) // 缓存有效 代码加入runSequence
            } else {
                this.jsRunSequence.push({ name: jslist[k].name, code: null, path: jslist[k].path, status: 'comboloading' }) //  缓存无效 代码加入运行队列 状态loading
                requestingModules[jslist[k].name] = true;
                updateList += (updateList == '' ? '' : ';') + jslist[k].path;
            }
        }
        var that = this;
        if (!!updateList) {
            var xhr = new XMLHttpRequest();
            xhr.open("get", combo + updateList, true);
            xhr.onreadystatechange = function () {
                if (xhr.readyState == 4) {
                    if ((xhr.status >= 200 && xhr.status < 300) || xhr.status == 304) {
                        if (xhr.response != '') {
                            that.runCombo(xhr.response, requestingModules);
                            return;
                        }
                    } else {
                        for (var i in that.jsRunSequence) {
                            if (requestingModules[that.jsRunSequence[i].name]) {
                                that.jsRunSequence[i].status = 'failed'
                            }
                        }
                        that.runjs();
                    }
                }
            };
            xhr.send(null);
        }

        this.runjs();
    }
    /*runcombo
     * comboCode 服务端返回的用/combojs/注释分隔开的js代码
     * requestingModules 所有被combo请求的modules map
     * requestingModules:{
     *       js文件name : true
     * }
     * combo服务返回代码后,用分隔符把所有js模块分隔成数组,
     * 用requestingModules查找jsRunSequence中该模块对应的项,
     * 更改该项,code为当前代码,status改为comboJS
     * 所有combo返回的模块遍历成功后,runjs()
     * runjs会把所有有代码的项当作成功项执行
     */
    lsloader.runCombo = function (comboCode, requestingModules) {
        comboCode = comboCode.split('/*combojs*/');
        comboCode.shift();//去除首个空code
        for (var k in this.jsRunSequence) {
            if (!!requestingModules[this.jsRunSequence[k].name] && !!comboCode[0]) {
                this.jsRunSequence[k].status = 'comboJS';
                this.jsRunSequence[k].code = comboCode[0];
                this.setLS(this.jsRunSequence[k].name, this.jsRunSequence[k].path + versionString + comboCode[0]);
                comboCode.shift();
            }
        }
        this.runjs();
    }

})()