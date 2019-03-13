(function ($) {
    /**
     * Simplified Chinese language package
     * Translated by @shamiao
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': '请输入有效的Base64编码'
        },
        between: {
            'default': '请输入在 %s 和 %s 之间的数值',
            notInclusive: '请输入在 %s 和 %s 之间(不含两端)的数值'
        },
        callback: {
            'default': '请输入有效的值'
        },
        choice: {
            'default': '请输入有效的值',
            less: '请至少选中 %s 个选项',
            more: '最多只能选中 %s 个选项',
            between: '请选择 %s 至 %s 个选项'
        },
        color: {
            'default': '请输入有效的颜色值'
        },
        creditCard: {
            'default': '请输入有效的信用卡号码'
        },
        cusip: {
            'default': '请输入有效的美国CUSIP代码'
        },
        cvv: {
            'default': '请输入有效的CVV代码'
        },
        date: {
            'default': '请输入有效的日期', 
            min: '请输入 %s 或之后的日期',
            max: '请输入 %s 或以前的日期',
            range: '请输入 %s 和 %s 之间的日期'
        },
        different: {
            'default': '请输入不同的值'
        },
        digits: {
            'default': '请输入有效的数字'
        },
        ean: {
            'default': '请输入有效的EAN商品编码'
        },
        emailAddress: {
            'default': '请输入有效的邮件地址'
        },
        file: {
            'default': '请选择有效的文件'
        },
        greaterThan: {
            'default': '请输入大于等于 %s 的数值',
            notInclusive: '请输入大于 %s 的数值'
        },
        grid: {
            'default': '请输入有效的GRId编码'
        },
        hex: {
            'default': '请输入有效的16进制数'
        },
        hexColor: {
            'default': '请输入有效的16进制颜色值'
        },
        iban: {
            'default': '请输入有效的IBAN(国际银行账户)号码',
            countryNotSupported: '不支持 %s 国家或地区',
            country: '请输入有效的 %s 国家或地区的IBAN(国际银行账户)号码',
            countries: {
                AD: '安道​​尔',
                AE: '阿联酋',
                AL: '阿尔巴尼亚',
                AO: '安哥拉',
                AT: '奥地利',
                AZ: '阿塞拜疆',
                BA: '波斯尼亚和黑塞哥维那',
                BE: '比利时',
                BF: '布基纳法索',
                BG: '保加利亚',
                BH: '巴林',
                BI: '布隆迪',
                BJ: '贝宁',
                BR: '巴西',
                CH: '瑞士',
                CI: '科特迪瓦',
                CM: '喀麦隆',
                CR: '哥斯达黎加',
                CV: '佛得角',
                CY: '塞浦路斯',
                CZ: '捷克共和国',
                DE: '德国',
                DK: '丹麦',
                DO: '多米尼加共和国',
                DZ: '阿尔及利亚',
                EE: '爱沙尼亚',
                ES: '西班牙',
                FI: '芬兰',
                FO: '法罗群岛',
                FR: '法国',
                GB: '英国',
                GE: '格鲁吉亚',
                GI: '直布罗陀',
                GL: '格陵兰岛',
                GR: '希腊',
                GT: '危地马拉',
                HR: '克罗地亚',
                HU: '匈牙利',
                IE: '爱尔兰',
                IL: '以色列',
                IR: '伊朗',
                IS: '冰岛',
                IT: '意大利',
                JO: '约旦',
                KW: '科威特',
                KZ: '哈萨克斯坦',
                LB: '黎巴嫩',
                LI: '列支敦士登',
                LT: '立陶宛',
                LU: '卢森堡',
                LV: '拉脱维亚',
                MC: '摩纳哥',
                MD: '摩尔多瓦',
                ME: '黑山',
                MG: '马达加斯加',
                MK: '马其顿',
                ML: '马里',
                MR: '毛里塔尼亚',
                MT: '马耳他',
                MU: '毛里求斯',
                MZ: '莫桑比克',
                NL: '荷兰',
                NO: '挪威',
                PK: '巴基斯坦',
                PL: '波兰',
                PS: '巴勒斯坦',
                PT: '葡萄牙',
                QA: '卡塔尔',
                RO: '罗马尼亚',
                RS: '塞尔维亚',
                SA: '沙特阿拉伯',
                SE: '瑞典',
                SI: '斯洛文尼亚',
                SK: '斯洛伐克',
                SM: '圣马力诺',
                SN: '塞内加尔',
                TN: '突尼斯',
                TR: '土耳其',
                VG: '英属维尔京群岛'
            }
        },
        id: {
            'default': '请输入有效的身份证件号码',
            countryNotSupported: '不支持 %s 国家或地区',
            country: '请输入有效的 %s 国家或地区的身份证件号码',
            countries: {
                BA: '波黑',
                BG: '保加利亚',
                BR: '巴西',
                CH: '瑞士',
                CL: '智利',
                CN: '中国',
                CZ: '捷克共和国',
                DK: '丹麦',
                EE: '爱沙尼亚',
                ES: '西班牙',
                FI: '芬兰',
                HR: '克罗地亚',
                IE: '爱尔兰',
                IS: '冰岛',
                LT: '立陶宛',
                LV: '拉脱维亚',
                ME: '黑山',
                MK: '马其顿',
                NL: '荷兰',
                RO: '罗马尼亚',
                RS: '塞尔维亚',
                SE: '瑞典',
                SI: '斯洛文尼亚',
                SK: '斯洛伐克',
                SM: '圣马力诺',
                TH: '泰国',
                ZA: '南非'
            }
        },
        identical: {
            'default': '请输入相同的值'
        },
        imei: {
            'default': '请输入有效的IMEI(手机串号)'
        },
        imo: {
            'default': '请输入有效的国际海事组织(IMO)号码'
        },
        integer: {
            'default': '请输入有效的整数值'
        },
        ip: {
            'default': '请输入有效的IP地址',
            ipv4: '请输入有效的IPv4地址',
            ipv6: '请输入有效的IPv6地址'
        },
        isbn: {
            'default': '请输入有效的ISBN(国际标准书号)'
        },
        isin: {
            'default': '请输入有效的ISIN(国际证券编码)'
        },
        ismn: {
            'default': '请输入有效的ISMN(印刷音乐作品编码)'
        },
        issn: {
            'default': '请输入有效的ISSN(国际标准杂志书号)'
        },
        lessThan: {
            'default': '请输入小于等于 %s 的数值',
            notInclusive: '请输入小于 %s 的数值'
        },
        mac: {
            'default': '请输入有效的MAC物理地址'
        },
        meid: {
            'default': '请输入有效的MEID(移动设备识别码)'
        },
        notEmpty: {
            'default': '请填写必填项目'
        },
        numeric: {
            'default': '请输入有效的数值，允许小数'
        },
        phone: {
            'default': '请输入有效的电话号码',
            countryNotSupported: '不支持 %s 国家或地区',
            country: '请输入有效的 %s 国家或地区的电话号码',
            countries: {
                BR: '巴西',
                CN: '中国',
                CZ: '捷克共和国',
                DE: '德国',
                DK: '丹麦',
                ES: '西班牙',
                FR: '法国',
                GB: '英国',
                MA: '摩洛哥',
                PK: '巴基斯坦',
                RO: '罗马尼亚',
                RU: '俄罗斯',
                SK: '斯洛伐克',
                TH: '泰国',
                US: '美国',
                VE: '委内瑞拉'
            }
        },
        regexp: {
            'default': '请输入符合正则表达式限制的值'
        },
        remote: {
            'default': '请输入有效的值'
        },
        rtn: {
            'default': '请输入有效的RTN号码'
        },
        sedol: {
            'default': '请输入有效的SEDOL代码'
        },
        siren: {
            'default': '请输入有效的SIREN号码'
        },
        siret: {
            'default': '请输入有效的SIRET号码'
        },
        step: {
            'default': '请输入在基础值上，增加 %s 的整数倍的数值'
        },
        stringCase: {
            'default': '只能输入小写字母',
            upper: '只能输入大写字母'
        },
        stringLength: {
            'default': '请输入符合长度限制的值',
            less: '最多只能输入 %s 个字符',
            more: '需要输入至少 %s 个字符',
            between: '请输入 %s 至 %s 个字符'
        },
        uri: {
            'default': '请输入一个有效的URL地址'
        },
        uuid: {
            'default': '请输入有效的UUID',
            version: '请输入版本 %s 的UUID'
        },
        vat: {
            'default': '请输入有效的VAT(税号)',
            countryNotSupported: '不支持 %s 国家或地区',
            country: '请输入有效的 %s 国家或地区的VAT(税号)',
            countries: {
                AT: '奥地利',
                BE: '比利时',
                BG: '保加利亚',
                BR: '巴西',
                CH: '瑞士',
                CY: '塞浦路斯',
                CZ: '捷克共和国',
                DE: '德国',
                DK: '丹麦',
                EE: '爱沙尼亚',
                ES: '西班牙',
                FI: '芬兰',
                FR: '法语',
                GB: '英国',
                GR: '希腊',
                EL: '希腊',
                HU: '匈牙利',
                HR: '克罗地亚',
                IE: '爱尔兰',
                IS: '冰岛',
                IT: '意大利',
                LT: '立陶宛',
                LU: '卢森堡',
                LV: '拉脱维亚',
                MT: '马耳他',
                NL: '荷兰',
                NO: '挪威',
                PL: '波兰',
                PT: '葡萄牙',
                RO: '罗马尼亚',
                RU: '俄罗斯',
                RS: '塞尔维亚',
                SE: '瑞典',
                SI: '斯洛文尼亚',
                SK: '斯洛伐克',
                VE: '委内瑞拉',
                ZA: '南非'
            }
        },
        vin: {
            'default': '请输入有效的VIN(美国车辆识别号码)'
        },
        zipCode: {
            'default': '请输入有效的邮政编码',
            countryNotSupported: '不支持 %s 国家或地区',
            country: '请输入有效的 %s 国家或地区的邮政编码',
            countries: {
                AT: '奥地利',
                BR: '巴西',
                CA: '加拿大',
                CH: '瑞士',
                CZ: '捷克共和国',
                DE: '德国',
                DK: '丹麦',
                FR: '法国',
                GB: '英国',
                IE: '爱尔兰',
                IT: '意大利',
                MA: '摩洛哥',
                NL: '荷兰',
                PT: '葡萄牙',
                RO: '罗马尼亚',
                RU: '俄罗斯',
                SE: '瑞典',
                SG: '新加坡',
                SK: '斯洛伐克',
                US: '美国'
            }
        }
    });
}(window.jQuery));
