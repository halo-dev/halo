(function ($) {
    /**
     * Italian language package
     * Translated by @maramazza
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Si prega di inserire un valore codificato in Base 64'
        },
        between: {
            'default': 'Si prega di inserire un valore tra %s e %s',
            notInclusive: 'Si prega di scegliere rigorosamente un valore tra %s e %s'
        },
        callback: {
            'default': 'Si prega di inserire un valore valido'
        },
        choice: {
            'default': 'Si prega di inserire un valore valido',
            less: 'Si prega di scegliere come minimo l\'opzione %s',
            more: 'Si prega di scegliere al massimo l\'opzione %s',
            between: 'Si prega di scegliere l\'opzione tra %s e %s'
        },
        color: {
            'default': 'Si prega di inserire un colore valido'
        },
        creditCard: {
            'default': 'Si prega di inserire un numero di carta di credito valido'
        },
        cusip: {
            'default': 'Si prega di inserire un numero CUSIP valido'
        },
        cvv: {
            'default': 'Si prega di inserire un numero CVV valido'
        },
        date: {
            'default': 'Si prega di inserire una data valida',
            min: 'Si prega di inserire una data successiva al %s',
            max: 'Si prega di inserire una data antecedente il %s',
            range: 'Si prega di inserire una data compresa tra %s - %s'
        },
        different: {
            'default': 'Si prega di inserire un valore differente'
        },
        digits: {
            'default': 'Si prega di inserire solo numeri'
        },
        ean: {
            'default': 'Si prega di inserire un numero EAN valido'
        },
        emailAddress: {
            'default': 'Si prega di inserire un indirizzo email valido'
        },
        file: {
            'default': 'Si prega di scegliere un file valido'
        },
        greaterThan: {
            'default': 'Si prega di inserire un numero maggiore o uguale a %s',
            notInclusive: 'Si prega di inserire un numero maggiore di %s'
        },
        grid: {
            'default': 'Si prega di inserire un numero GRId valido'
        },
        hex: {
            'default': 'Si prega di inserire un numero esadecimale valido'
        },
        hexColor: {
            'default': 'Si prega di inserire un hex colore valido'
        },
        iban: {
            'default': 'Si prega di inserire un numero IBAN valido',
            countryNotSupported: 'Il codice del paese %s non e supportato',
            country: 'Si prega di inserire un numero IBAN valido per %s',
            countries: {
                AD: 'Andorra',
                AE: 'Emirati Arabi Uniti',
                AL: 'Albania',
                AO: 'Angola',
                AT: 'Austria',
                AZ: 'Azerbaijan',
                BA: 'Bosnia-Erzegovina',
                BE: 'Belgio',
                BF: 'Burkina Faso',
                BG: 'Bulgaria',
                BH: 'Bahrain',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brasile',
                CH: 'Svizzera',
                CI: 'Costa d\'Avorio',
                CM: 'Cameron',
                CR: 'Costa Rica',
                CV: 'Capo Verde',
                CY: 'Cipro',
                CZ: 'Republica Ceca',
                DE: 'Germania',
                DK: 'Danimarca',
                DO: 'Repubblica Domenicana',
                DZ: 'Algeria',
                EE: 'Estonia',
                ES: 'Spagna',
                FI: 'Finlandia',
                FO: 'Isole Faroe',
                FR: 'Francia',
                GB: 'Regno Unito',
                GE: 'Georgia',
                GI: 'Gibilterra',
                GL: 'Groenlandia',
                GR: 'Grecia',
                GT: 'Guatemala',
                HR: 'Croazia',
                HU: 'Ungheria',
                IE: 'Irlanda',
                IL: 'Israele',
                IR: 'Iran',
                IS: 'Islanda',
                IT: 'Italia',
                JO: 'Giordania',
                KW: 'Kuwait',
                KZ: 'Kazakhstan',
                LB: 'Libano',
                LI: 'Liechtenstein',
                LT: 'Lituania',
                LU: 'Lussemburgo',
                LV: 'Lettonia',
                MC: 'Monaco',
                MD: 'Moldavia',
                ME: 'Montenegro',
                MG: 'Madagascar',
                MK: 'Macedonia',
                ML: 'Mali',
                MR: 'Mauritania',
                MT: 'Malta',
                MU: 'Mauritius',
                MZ: 'Mozambico',
                NL: 'Olanda',
                NO: 'Norvegia',
                PK: 'Pachistan',
                PL: 'Polonia',
                PS: 'Palestina',
                PT: 'Portogallo',
                QA: 'Qatar',
                RO: 'Romania',
                RS: 'Serbia',
                SA: 'Arabia Saudita',
                SE: 'Svezia',
                SI: 'Slovenia',
                SK: 'Slovacchia',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunisia',
                TR: 'Turchia',
                VG: 'Isole Vergini, Inghilterra'
            }
        },
        id: {
            'default': 'Si prega di inserire un numero di identificazione valido',
            countryNotSupported: 'Il codice nazione %s non e supportato',
            country: 'Si prega di inserire un numero di identificazione valido per %s',
            countries: {
                BA: 'Bosnia-Erzegovina',
                BG: 'Bulgaria',
                BR: 'Brasile',
                CH: 'Svizzera',
                CL: 'Chile',
                CN: 'Cina',
                CZ: 'Republica Ceca',
                DK: 'Danimarca',
                EE: 'Estonia',
                ES: 'Spagna',
                FI: 'Finlandia',
                HR: 'Croazia',
                IE: 'Irlanda',
                IS: 'Islanda',
                LT: 'Lituania',
                LV: 'Lettonia',
                ME: 'Montenegro',
                MK: 'Macedonia',
                NL: 'Paesi Bassi',
                RO: 'Romania',
                RS: 'Serbia',
                SE: 'Svezia',
                SI: 'Slovenia',
                SK: 'Slovacchia',
                SM: 'San Marino',
                TH: 'Thailandia',
                ZA: 'Sudafrica'
            }
        },
        identical: {
            'default': 'Si prega di inserire un valore identico'
        },
        imei: {
            'default': 'Si prega di inserire un numero IMEI valido'
        },
        imo: {
            'default': 'Si prega di inserire un numero IMO valido'
        },
        integer: {
            'default': 'Si prega di inserire un numero valido'
        },
        ip: {
            'default': 'Please enter a valid IP address',
            ipv4: 'Si prega di inserire un indirizzo IPv4 valido',
            ipv6: 'Si prega di inserire un indirizzo IPv6 valido'
        },
        isbn: {
            'default': 'Si prega di inserire un numero ISBN valido'
        },
        isin: {
            'default': 'Si prega di inserire un numero ISIN valido'
        },
        ismn: {
            'default': 'Si prega di inserire un numero ISMN valido'
        },
        issn: {
            'default': 'Si prega di inserire un numero ISSN valido'
        },
        lessThan: {
            'default': 'Si prega di inserire un valore minore o uguale a %s',
            notInclusive: 'Si prega di inserire un valore minore di %s'
        },
        mac: {
            'default': 'Si prega di inserire un valido MAC address'
        },
        meid: {
            'default': 'Si prega di inserire un numero MEID valido'
        },
        notEmpty: {
            'default': 'Si prega di non lasciare il campo vuoto'
        },
        numeric: {
            'default': 'Si prega di inserire un numero con decimali valido'
        },
        phone: {
            'default': 'Si prega di inserire un numero di telefono valido',
            countryNotSupported: 'Il codice nazione %s non e supportato',
            country: 'Si prega di inserire un numero di telefono valido per %s',
            countries: {
                BR: 'Brasile',
                CN: 'Cina',
                CZ: 'Republica Ceca',
                DE: 'Germania',
                DK: 'Danimarca',
                ES: 'Spagna',
                FR: 'Francia',
                GB: 'Regno Unito',
                MA: 'Marocco',
                PK: 'Pakistan',
                RO: 'Romania',
                RU: 'Russia',
                SK: 'Slovacchia',
                TH: 'Thailandia',
                US: 'Stati Uniti d\'America',
                VE: 'Venezuelano'
            }
        },
        regexp: {
            'default': 'Inserisci un valore che corrisponde al modello'
        },
        remote: {
            'default': 'Si prega di inserire un valore valido'
        },
        rtn: {
            'default': 'Si prega di inserire un numero RTN valido'
        },
        sedol: {
            'default': 'Si prega di inserire un numero SEDOL valido'
        },
        siren: {
            'default': 'Si prega di inserire un numero SIREN valido'
        },
        siret: {
            'default': 'Si prega di inserire un numero SIRET valido'
        },
        step: {
            'default': 'Si prega di inserire uno step valido di %s'
        },
        stringCase: {
            'default': 'Si prega di inserire solo caratteri minuscoli',
            upper: 'Si prega di inserire solo caratteri maiuscoli'
        },
        stringLength: {
            'default': 'Si prega di inserire un valore con lunghezza valida',
            less: 'Si prega di inserire meno di %s caratteri',
            more: 'Si prega di inserire piu di %s caratteri',
            between: 'Si prega di inserire un numero di caratteri compreso tra  %s e %s'
        },
        uri: {
            'default': 'Si prega di inserire un URI valido'
        },
        uuid: {
            'default': 'Si prega di inserire un numero UUID valido',
            version: 'Si prega di inserire un numero di versione UUID %s valido'
        },
        vat: {
            'default': 'Si prega di inserire un valore di IVA valido',
            countryNotSupported: 'Il codice nazione %s non e supportato',
            country: 'Si prega di inserire un valore di IVA valido per %s',
            countries: {
                AT: 'Austria',
                BE: 'Belgio',
                BG: 'Bulgaria',
                BR: 'Brasiliano',
                CH: 'Svizzera',
                CY: 'Cipro',
                CZ: 'Republica Ceca',
                DE: 'Germania',
                DK: 'Danimarca',
                EE: 'Estonia',
                ES: 'Spagna',
                FI: 'Finlandia',
                FR: 'Francia',
                GB: 'Regno Unito',
                GR: 'Grecia',
                EL: 'Grecia',
                HU: 'Ungheria',
                HR: 'Croazia',
                IE: 'Irlanda',
                IS: 'Islanda',
                IT: 'Italia',
                LT: 'Lituania',
                LU: 'Lussemburgo',
                LV: 'Lettonia',
                MT: 'Malta',
                NL: 'Olanda',
                NO: 'Norvegia',
                PL: 'Polonia',
                PT: 'Portogallo',
                RO: 'Romania',
                RU: 'Russia',
                RS: 'Serbia',
                SE: 'Svezia',
                SI: 'Slovenia',
                SK: 'Slovacchia',
                VE: 'Venezuelano',
                ZA: 'Sud Africano'
            }
        },
        vin: {
            'default': 'Si prega di inserire un numero VIN valido'
        },
        zipCode: {
            'default': 'Si prega di inserire un codice postale valido',
            countryNotSupported: 'Il codice nazione %s non e supportato',
            country: 'Si prega di inserire un codice postale valido per %s',
            countries: {
                AT: 'Austria',
                BR: 'Brasile',
                CA: 'Canada',
                CH: 'Svizzera',
                CZ: 'Republica Ceca',
                DE: 'Germania',
                DK: 'Danimarca',
                FR: 'Francia',
                GB: 'Regno Unito',
                IE: 'Irlanda',
                IT: 'Italia',
                MA: 'Marocco',
                NL: 'Paesi Bassi',
                PT: 'Portogallo',
                RO: 'Romania',
                RU: 'Russia',
                SE: 'Svezia',
                SG: 'Singapore',
                SK: 'Slovacchia',
                US: 'Stati Uniti d\'America'
            }
        }
    });
}(window.jQuery));
