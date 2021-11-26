import CryptoJS from 'crypto-js'

const CRYPTO_KEY = 'halo-crypt'

export default {
  encrypt(plaintObject) {
    if (!plaintObject) {
      return undefined
    }
    return CryptoJS.AES.encrypt(JSON.stringify(plaintObject), CRYPTO_KEY).toString()
  },

  decrypt(ciphertext) {
    if (!ciphertext) {
      return undefined
    }
    const bytes = CryptoJS.AES.decrypt(ciphertext, CRYPTO_KEY)
    return JSON.parse(bytes.toString(CryptoJS.enc.Utf8))
  }
}
