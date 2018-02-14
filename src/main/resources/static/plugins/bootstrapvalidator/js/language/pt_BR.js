(function ($) {
    /**
     * Portuguese (Brazil) language package
     * Translated by @marcuscarvalho6. Improved by @dgmike
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Por favor insira um código base 64 válido'
        },
        between: {
            'default': 'Por favor insira um valor entre %s e %s',
            notInclusive: 'Por favor insira um valor estritamente entre %s e %s'
        },
        callback: {
            'default': 'Por favor insira um valor válido'
        },
        choice: {
            'default': 'Por favor insira um valor válido',
            less: 'Por favor escolha %s opções no mínimo',
            more: 'Por favor escolha %s opções no máximo',
            between: 'Por favor escolha de %s a %s opções'
        },
        color: {
            'default': 'Por favor insira uma cor válida'
        },
        creditCard: {
            'default': 'Por favor insira um número de cartão de crédito válido'
        },
        cusip: {
            'default': 'Por favor insira um número CUSIP válido'
        },
        cvv: {
            'default': 'Por favor insira um número CVV válido'
        },
        date: {
            'default': 'Por favor insira uma data válida',
            min: 'Por favor insira uma data posterior a %s',
            max: 'Por favor insira uma data anterior a %s',
            range: 'Por favor insira uma data entre %s e %s'
        },
        different: {
            'default': 'Por favor insira valores diferentes'
        },
        digits: {
            'default': 'Por favor insira somente dígitos'
        },
        ean: {
            'default': 'Por favor insira um número EAN válido'
        },
        emailAddress: {
            'default': 'Por favor insira um email válido'
        },
        file: {
            'default': 'Por favor escolha um arquivo válido'
        },
        greaterThan: {
            'default': 'Por favor insira um valor maior ou igual a %s',
            notInclusive: 'Por favor insira um valor maior do que %s'
        },
        grid: {
            'default': 'Por favor insira uma GRID válida'
        },
        hex: {
            'default': 'Por favor insira um hexadecimal válido'
        },
        hexColor: {
            'default': 'Por favor insira uma cor hexadecimal válida'
        },
        iban: {
            'default': 'Por favor insira um número IBAN válido',
            countryNotSupported: 'O código do país %s não é suportado',
            country: 'Por favor insira um número IBAN válido em %s',
            countries: {
                AD: 'Andorra',
                AE: 'Emirados Árabes',
                AL: 'Albânia',
                AO: 'Angola',
                AT: 'Áustria',
                AZ: 'Azerbaijão',
                BA: 'Bósnia-Herzegovina',
                BE: 'Bélgica',
                BF: 'Burkina Faso',
                BG: 'Bulgária',
                BH: 'Bahrain',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brasil',
                CH: 'Suíça',
                IC: 'Costa do Marfim',
                CM: 'Camarões',
                CR: 'Costa Rica',
                CV: 'Cabo Verde',
                CY: 'Chipre',
                CZ: 'República Checa',
                DE: 'Alemanha',
                DK: 'Dinamarca',
                DO: 'República Dominicana',
                DZ: 'Argélia',
                EE: 'Estónia',
                ES: 'Espanha',
                FI: 'Finlândia',
                FO: 'Ilhas Faroé',
                FR: 'França',
                GB: 'Reino Unido',
                GE: 'Georgia',
                GI: 'Gibraltar',
                GL: 'Groenlândia',
                GR: 'Grécia',
                GT: 'Guatemala',
                HR: 'Croácia',
                HU: 'Hungria',
                IE: 'Ireland',
                IL: 'Israel',
                IR: 'Irão',
                IS: 'Islândia',
                TI: 'Itália',
                JO: 'Jordan',
                KW: 'Kuwait',
                KZ: 'Cazaquistão',
                LB: 'Líbano',
                LI: 'Liechtenstein',
                LT: 'Lituânia',
                LU: 'Luxemburgo',
                LV: 'Letónia',
                MC: 'Mônaco',
                MD: 'Moldávia',
                ME: 'Montenegro',
                MG: 'Madagascar',
                MK: 'Macedónia',
                ML: 'Mali',
                MR: 'Mauritânia',
                MT: 'Malta',
                MU: 'Maurício',
                MZ: 'Moçambique',
                NL: 'Países Baixos',
                NO: 'Noruega',
                PK: 'Paquistão',
                PL: 'Polônia',
                PS: 'Palestino',
                PT: 'Portugal',
                QA: 'Qatar',
                RO: 'Roménia',
                RS: 'Sérvia',
                SA: 'Arábia Saudita',
                SE: 'Suécia',
                SI: 'Eslovénia',
                SK: 'Eslováquia',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunísia',
                TR: 'Turquia',
                VG: 'Ilhas Virgens Britânicas'
            }
        },
        id: {
            'default': 'Por favor insira um código de identificação válido',
            countryNotSupported: 'O código do país %s não é suportado',
            country: 'Por favor insira um número de indentificação válido em %s',
            countries: {
                BA: 'Bósnia e Herzegovina',
                BG: 'Bulgária',
                BR: 'Brasil',
                CH: 'Suíça',
                CL: 'Chile',
                CN: 'China',
                CZ: 'República Checa',
                DK: 'Dinamarca',
                EE: 'Estônia',
                ES: 'Espanha',
                FI: 'Finlândia',
                HR: 'Croácia',
                IE: 'Irlanda',
                IS: 'Islândia',
                LT: 'Lituânia',
                LV: 'Letónia',
                ME: 'Montenegro',
                MK: 'Macedónia',
                NL: 'Holanda',
                RO: 'Roménia',
                RS: 'Sérvia',
                SE: 'Suécia',
                SI: 'Eslovênia',
                SK: 'Eslováquia',
                SM: 'San Marino',
                TH: 'Tailândia',
                ZA: 'África do Sul'
            }
        },
        identical: {
            'default': 'Por favor, insira o mesmo valor'
        },
        imei: {
            'default': 'Por favor insira um IMEI válido'
        },
        imo: {
            'default': 'Por favor insira um IMO válido'
        },
        integer: {
            'default': 'Por favor insira um número inteiro válido'
        },
        ip: {
            'default': 'Por favor insira um IP válido',
            ipv4: 'Por favor insira um endereço de IPv4 válido',
            ipv6: 'Por favor insira um endereço de IPv6 válido'
        },
        isbn: {
            'default': 'Por favor insira um ISBN válido'
        },
        isin: {
            'default': 'Por favor insira um ISIN válido'
        },
        ismn: {
            'default': 'Por favor insira um ISMN válido'
        },
        issn: {
            'default': 'Por favor insira um ISSN válido'
        },
        lessThan: {
            'default': 'Por favor insira um valor menor ou igual a %s',
            notInclusive: 'Por favor insira um valor menor do que %s'
        },
        mac: {
            'default': 'Por favor insira um endereço MAC válido'
        },
        meid: {
            'default': 'Por favor insira um MEID válido'
        },
        notEmpty: {
            'default': 'Por favor insira um valor'
        },
        numeric: {
            'default': 'Por favor insira um número real válido'
        },
        phone: {
            'default': 'Por favor insira um número de telefone válido',
            countryNotSupported: 'O código de país %s não é suportado',
            country: 'Por favor insira um número de telefone válido em %s',
            countries: {
                BR: 'Brasil',
                CN: 'China',
                CZ: 'República Checa',
                DE: 'Alemanha',
                DK: 'Dinamarca',
                ES: 'Espanha',
                FR: 'França',
                GB: 'Reino Unido',
                MA: 'Marrocos',
                PK: 'Paquistão',
                RO: 'Roménia',
                RU: 'Rússia',
                SK: 'Eslováquia',
                TH: 'Tailândia',
                US: 'EUA',
                VE: 'Venezuela'
            }
        },
        regexp: {
            'default': 'Por favor insira um valor correspondente ao padrão'
        },
        remote: {
            'default': 'Por favor insira um valor válido'
        },
        rtn: {
            'default': 'Por favor insira um número válido RTN'
        },
        sedol: {
            'default': 'Por favor insira um número válido SEDOL'
        },
        siren: {
            'default': 'Por favor insira um número válido SIREN'
        },
        siret: {
            'default': 'Por favor insira um número válido SIRET'
        },
        step: {
            'default': 'Por favor insira um passo válido %s'
        },
        stringCase: {
            'default': 'Por favor, digite apenas caracteres minúsculos',
            upper: 'Por favor, digite apenas caracteres maiúsculos'
        },
        stringLength: {
            'default': 'Por favor insira um valor com comprimento válido',
            less: 'Por favor insira menos de %s caracteres',
            more: 'Por favor insira mais de %s caracteres',
            between: 'Por favor insira um valor entre %s e %s caracteres'
        },
        uri: {
            'default': 'Por favor insira um URI válido'
        },
        uuid: {
            'default': 'Por favor insira um número válido UUID',
            version: 'Por favor insira uma versão %s  UUID válida'
        },
        vat: {
            'default': 'Por favor insira um VAT válido',
            countryNotSupported: 'O código do país %s não é suportado',
            country: 'Por favor insira um número VAT válido em %s',
            countries: {
                AT: 'Áustria',
                BE: 'Bélgica',
                BG: 'Bulgária',
                BR: 'Brasil',
                CH: 'Suíça',
                CY: 'Chipre',
                CZ: 'República Checa',
                DE: 'Alemanha',
                DK: 'Dinamarca',
                EE: 'Estônia',
                ES: 'Espanha',
                FI: 'Finlândia',
                FR: 'França',
                GB: 'Reino Unido',
                GR: 'Grécia',
                EL: 'Grécia',
                HU: 'Hungria',
                HR: 'Croácia',
                IE: 'Irlanda',
                IS: 'Islândia',
                IT: 'Itália',
                LT: 'Lituânia',
                LU: 'Luxemburgo',
                LV: 'Letónia',
                MT: 'Malta',
                NL: 'Holanda',
                NO: 'Norway',
                PL: 'Polônia',
                PT: 'Portugal',
                RO: 'Roménia',
                RU: 'Rússia',
                RS: 'Sérvia',
                SE: 'Suécia',
                SI: 'Eslovênia',
                SK: 'Eslováquia',
                VE: 'Venezuela',
                ZA: 'África do Sul'
            }
        },
        vin: {
            'default': 'Por favor insira um VIN válido'
        },
        zipCode: {
            'default': 'Por favor insira um código postal válido',
            countryNotSupported: 'O código postal do país %s não é suportado',
            country: 'Por favor insira um código postal válido em %s',
            countries: {
                AT: 'Áustria',
                BR: 'Brasil',
                CA: 'Canadá',
                CH: 'Suíça',
                CZ: 'República Checa',
                DE: 'Alemanha',
                DK: 'Dinamarca',
                FR: 'França',
                GB: 'Reino Unido',
                IE: 'Irlanda',
                IT: 'Itália',
                MA: 'Marrocos',
                NL: 'Holanda',
                PT: 'Portugal',
                RO: 'Roménia',
                RU: 'Rússia',
                SE: 'Suécia',
                SG: 'Cingapura',
                SK: 'Eslováquia',
                US: 'EUA'
            }
        }
    });
}(window.jQuery));
