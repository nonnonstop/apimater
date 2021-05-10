"use strict";
var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (Object.prototype.hasOwnProperty.call(b, p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        if (typeof b !== "function" && b !== null)
            throw new TypeError("Class extends value " + String(b) + " is not a constructor or null");
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
/**
 * Translate to DAT file
 *
 * This function is called from LoadActivity on worker thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function translate(context, htmlUrl) {
    var datWriter = new DatWriter(context);
    // Avaiable translators: HtmlToDatTranslator, ScToNetTranslator, ApiToDatTranslator
    var translator = new HtmlToDatTranslator(htmlUrl, datWriter);
    // const translator: Translator = new ScToNetTranslator(htmlUrl, datWriter);
    // const translator: Translator = new ApiToDatTranslator(htmlUrl, datWriter);
    translator.translate();
}
/**
 * Base class of dat translator
 */
var Translator = /** @class */ (function () {
    function Translator(htmlUrl, writer) {
        var urlMatcher = htmlUrl.match(/^https?:\/\/(\w+)\.[\w.]+\/test\/read\.(?:cgi|php)\/(\w+)\/(\d+)/);
        if (urlMatcher === null) {
            throw new Error('Unexpected URL');
        }
        this.htmlUrl = urlMatcher[0];
        this.server = urlMatcher[1];
        this.board = urlMatcher[2];
        this.thread = urlMatcher[3];
        this.datFilename = this.board + "_" + this.thread + ".dat";
        this.writer = writer;
    }
    Translator.prototype.openDat = function () {
        this.writer.open(this.datFilename);
    };
    Translator.prototype.writeDat = function (str) {
        this.writer.write(str);
    };
    Translator.prototype.closeDat = function () {
        this.writer.close();
    };
    return Translator;
}());
/**
 * Dat translator using 5ch html
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
var HtmlToDatTranslator = /** @class */ (function (_super) {
    __extends(HtmlToDatTranslator, _super);
    function HtmlToDatTranslator() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    HtmlToDatTranslator.prototype.requestHtml = function () {
        var client = new Packages.okhttp3.OkHttpClient();
        var request = new Packages.okhttp3.Request.Builder()
            .url(this.htmlUrl)
            .build();
        var responseCharset = Packages.java.nio.charset.Charset.forName('MS932');
        var response = client.newCall(request).execute();
        try {
            if (response.code() !== 200) {
                throw new Error('Failed to connect');
            }
            var body = response.body().source().readString(responseCharset);
            return body + ''; // convert Java String to JS String
        }
        finally {
            response.close();
        }
    };
    HtmlToDatTranslator.prototype.convertToDat = function (htmlData) {
        if (htmlData.indexOf('<div class="topmenu">') != -1) {
            this.convertToDatV6(htmlData);
        }
        else if (htmlData.indexOf('<div class="topmenu centered">') != -1) {
            this.convertToDatV7(htmlData);
        }
        else if (htmlData.indexOf('<ul class="topmenu">') != -1) {
            this.convertToDatPink(htmlData);
        }
        else {
            this.convertToDatV5(htmlData);
        }
    };
    HtmlToDatTranslator.prototype.convertToDatV5 = function (htmlData) {
        var _a, _b, _c, _d;
        var title = ((_b = (_a = htmlData.match(/<title>(.*?) ?<\/title>/)) === null || _a === void 0 ? void 0 : _a[1]) !== null && _b !== void 0 ? _b : '') + '\t';
        var resRegexp = /<dt>(\d+) ：(?:<a href="mailto:([^"]*)">(.*?)<\/a>|<font color=green>(.*?)<\/font>)：(.*?)<dd>(.*)<br><br>/g;
        try {
            for (;;) {
                var res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                var mail = (_c = res[2]) !== null && _c !== void 0 ? _c : '';
                var name_1 = ((_d = res[3]) !== null && _d !== void 0 ? _d : res[4])
                    .replace(/^<b>(.*)<\/b>$/g, '$1');
                var date = res[5]
                    .replace(/<a href="javascript:be\((\d+)\);">\?([^<]+)<\/a>/g, 'BE:$1-$2');
                var message = res[6]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="http(:\/\/[^"]+)">/g, 'sssp$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(mail + "<>" + name_1 + "<>" + date + "<>" + message + "<>" + title + "\n");
                    title = null;
                }
                else {
                    this.writeDat(mail + "<>" + name_1 + "<>" + date + "<>" + message + "<>\n");
                }
            }
        }
        finally {
            this.closeDat();
        }
    };
    HtmlToDatTranslator.prototype.convertToDatV6 = function (htmlData) {
        var _a, _b, _c;
        var title = ((_b = (_a = htmlData.match(/<title>(.*?)\n<\/title>/)) === null || _a === void 0 ? void 0 : _a[1]) !== null && _b !== void 0 ? _b : '') + '\t';
        var resRegexp = /<div class="post"[^>]*><div class="number">(?!0)(\d+) : <\/div><div class="name"><b>(?:<a href="mailto:([^"]*)">)?(.*?)(?:<\/a>)?<\/b><\/div><div class="date">(.*?)<\/div><div class="message">(.*?)<\/div><\/div>/g;
        try {
            for (;;) {
                var res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                var mail = (_c = res[2]) !== null && _c !== void 0 ? _c : '';
                var name_2 = res[3];
                var date = res[4]
                    .replace(/<\/div><div class="be [^"]+"><a href="http:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2');
                var message = res[5]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="(\/\/[^"]+)">/g, 'sssp:$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(mail + "<>" + name_2 + "<>" + date + "<>" + message + "<>" + title + "\n");
                    title = null;
                }
                else {
                    this.writeDat(mail + "<>" + name_2 + "<>" + date + "<>" + message + "<>\n");
                }
            }
        }
        finally {
            this.closeDat();
        }
    };
    HtmlToDatTranslator.prototype.convertToDatV7 = function (htmlData) {
        var _a, _b, _c;
        var title = ((_b = (_a = htmlData.match(/<title>(.*?)\n<\/title>/)) === null || _a === void 0 ? void 0 : _a[1]) !== null && _b !== void 0 ? _b : '') + '\t';
        var resRegexp = /<div class="post"[^>]*><div class="meta"><span class="number">0*(\d+)<\/span><span class="name"><b>(?:<a href="mailto:([^"]*)">)?(.*?)(?:<\/a>)?<\/b><\/span><span class="date">(.*?)<\/span><span class="uid">(.*?)<\/span><\/div><div class="message"><span class="escaped">(.*?)<\/span><\/div><\/div>/g;
        try {
            for (;;) {
                var res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                var mail = (_c = res[2]) !== null && _c !== void 0 ? _c : '';
                var name_3 = res[3];
                var date = res[4] + ' ' + res[5]
                    .replace(/<\/span><span class="be [^"]+"><a href="http:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2');
                var message = res[6]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="(\/\/[^"]+)">/g, 'sssp:$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(mail + "<>" + name_3 + "<>" + date + "<>" + message + "<>" + title + "\n");
                    title = null;
                }
                else {
                    this.writeDat(mail + "<>" + name_3 + "<>" + date + "<>" + message + "<>\n");
                }
            }
        }
        finally {
            this.closeDat();
        }
    };
    HtmlToDatTranslator.prototype.convertToDatPink = function (htmlData) {
        var _a, _b, _c, _d;
        var title = (_b = (_a = htmlData.match(/<title>(.*?)\n<\/title>/)) === null || _a === void 0 ? void 0 : _a[1]) !== null && _b !== void 0 ? _b : '';
        var resRegexp = /<dl class="post"[^>]*><dt class=""><span class="number">(?!0)(\d+) : <\/span><span class="name"><b>(?:<a href="mailto:([^"]*)">(.*?)<\/a>|<font color="green">(.*?)<\/font>)<\/b><\/span><span class="date">(.*?)<\/(?:span|div)><\/dt><dd class="thread_in">(.*?)<\/dd><\/dl>/g;
        try {
            for (;;) {
                var res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                var mail = (_c = res[2]) !== null && _c !== void 0 ? _c : '';
                var name_4 = (_d = res[3]) !== null && _d !== void 0 ? _d : res[4];
                var date = res[5]
                    .replace(/<\/span><div class="be [^"]+"><a href="https:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2');
                var message = res[6]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="http(:\/\/[^"]+)">/g, 'sssp$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(mail + "<>" + name_4 + "<>" + date + "<>" + message + "<>" + title + "\n");
                    title = null;
                }
                else {
                    this.writeDat(mail + "<>" + name_4 + "<>" + date + "<>" + message + "<>\n");
                }
            }
        }
        finally {
            this.closeDat();
        }
    };
    HtmlToDatTranslator.prototype.translate = function () {
        var htmlData = this.requestHtml();
        this.convertToDat(htmlData);
    };
    return HtmlToDatTranslator;
}(Translator));
/**
 * Dat translator using 2ch.sc dat
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
var ScToNetTranslator = /** @class */ (function (_super) {
    __extends(ScToNetTranslator, _super);
    function ScToNetTranslator() {
        return _super !== null && _super.apply(this, arguments) || this;
    }
    ScToNetTranslator.prototype.requestScDat = function () {
        var urlMatcher = this.htmlUrl.match(/^https?:\/\/(\w+)\.[\w.]+\/test\/read\.(?:cgi|php)\/(\w+)\/(\d+)/);
        if (urlMatcher === null) {
            throw new Error('Unexpected URL');
        }
        var datUrl = "http://" + urlMatcher[1] + ".2ch.sc/" + urlMatcher[2] + "/dat/" + urlMatcher[3] + ".dat";
        var client = new Packages.okhttp3.OkHttpClient();
        var request = new Packages.okhttp3.Request.Builder()
            .url(datUrl)
            .build();
        var responseCharset = Packages.java.nio.charset.Charset.forName('MS932');
        var response = client.newCall(request).execute();
        try {
            if (response.code() !== 200) {
                throw new Error('Failed to connect');
            }
            var body = response.body().source().readString(responseCharset);
            return body + ''; // convert Java String to JS String
        }
        finally {
            response.close();
        }
    };
    ScToNetTranslator.prototype.convertToNetDat = function (scDat) {
        try {
            var datLines = scDat.split(/\r\n|\n|\r/);
            this.openDat();
            for (var _i = 0, datLines_1 = datLines; _i < datLines_1.length; _i++) {
                var datLine = datLines_1[_i];
                var res = datLine.split('<>');
                if (res.length < 4)
                    break;
                res[0] = res[0]
                    .replace(/＠＼(^o^)／/g, '＠無断転載禁止');
                res[2] = res[2]
                    .replace(/<a href="http:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2')
                    .replace(/( ID:[^ .]+)($| )/g, '$1.sc$2')
                    .replace(/(\.\d+)($| (?!ID:)(?!.*\.net))/g, '$1 .sc$2')
                    .replace(/\.net/g, '');
                res[3] = res[3]
                    .replace(/sssp:\/\/img\.2ch\.sc\/ico\//g, 'sssp://img.5ch.sc/ico/')
                    .replace(/<img src="(\/\/[^"]+)">/g, 'sssp:$1');
                this.writeDat(res.join('<>') + '\n');
            }
        }
        finally {
            this.closeDat();
        }
    };
    ScToNetTranslator.prototype.translate = function () {
        var scDat = this.requestScDat();
        this.convertToNetDat(scDat);
    };
    return ScToNetTranslator;
}(Translator));
/**
 * Dat translator using API
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
var ApiToDatTranslator = /** @class */ (function (_super) {
    __extends(ApiToDatTranslator, _super);
    function ApiToDatTranslator(htmlUrl, writer) {
        var _this = _super.call(this, htmlUrl, writer) || this;
        var hmkey = ApiToDatTranslator.toBytes(ApiToDatTranslator.HM_KEY);
        var secret = new Packages.javax.crypto.spec.SecretKeySpec(hmkey, 'HmacSHA256');
        _this.mac = Packages.javax.crypto.Mac.getInstance('HmacSHA256');
        _this.mac.init(secret);
        return _this;
    }
    ApiToDatTranslator.prototype.request = function (path) {
        var message = ApiToDatTranslator.toBytes(path + ApiToDatTranslator.SID + ApiToDatTranslator.APP_KEY);
        var hobo = Packages.java.lang.String.format('%064x', new Packages.java.math.BigInteger(1, this.mac.doFinal(message)));
        var client = new Packages.okhttp3.OkHttpClient();
        var body = new Packages.okhttp3.FormBody.Builder()
            .add('sid', ApiToDatTranslator.SID)
            .add('hobo', hobo)
            .add('appkey', ApiToDatTranslator.APP_KEY)
            .build();
        var request = new Packages.okhttp3.Request.Builder()
            .url('https://kg.dev5ch.net' + path)
            .addHeader('User-Agent', 'Monazilla/1.00 2chMate/0.8.10.89 Dalvik/2.1.0 (Linux; U; Android 10; Pixel 3a Build/QQ2A.200305.002)')
            .addHeader('X-2ch-UA', '2chMate/0.8.10.89')
            .post(body)
            .build();
        var responseCharset = Packages.java.nio.charset.Charset.forName('MS932');
        var response = client.newCall(request).execute();
        try {
            if (response.code() !== 200) {
                throw new Error('Failed to connect');
            }
            var body_1 = response.body().source().readString(responseCharset);
            return body_1 + ''; // convert Java String to JS String
        }
        finally {
            response.close();
        }
    };
    ApiToDatTranslator.toBytes = function (str) {
        var bytes = [];
        var length = str.length;
        for (var i = 0; i < length; ++i) {
            var code = str.charCodeAt(i);
            bytes.push(code);
        }
        return bytes;
    };
    ApiToDatTranslator.prototype.translate = function () {
        var id = JSON.parse(this.request("/api/v1/prepare/" + this.server + "/" + this.board + "/" + this.thread)).id;
        Packages.java.lang.Thread.sleep(5000);
        try {
            var dat = this.request("/api/v1/get/" + id);
            this.openDat();
            this.writeDat(dat);
        }
        finally {
            this.closeDat();
        }
    };
    ApiToDatTranslator.APP_KEY = '<2CH APP KEY>';
    ApiToDatTranslator.HM_KEY = '<2CH HM KEY>';
    ApiToDatTranslator.SID = 'AbCdEfgHijklMnopqrsTuvwxyz';
    return ApiToDatTranslator;
}(Translator));
/**
 * Dat writer
 */
var DatWriter = /** @class */ (function () {
    function DatWriter(context) {
        this.context = context;
    }
    DatWriter.prototype.open = function (filename) {
        var _a;
        var baseUriStr = this.context.getSharedPreferences('DatWriter', 0).getString('documentTreeUri', null);
        if (baseUriStr === null) {
            throw new Error('Dat directory is not initialized');
        }
        var baseUri = Packages.android.net.Uri.parse(baseUriStr);
        var baseDir = Packages.androidx.documentfile.provider.DocumentFile.fromTreeUri(this.context, baseUri);
        if (baseDir === null) {
            throw new Error('Failed to open dat directory');
        }
        var file = (_a = baseDir.findFile(filename)) !== null && _a !== void 0 ? _a : baseDir.createFile('application/octet-stream', filename);
        if (baseDir === null) {
            throw new Error('Failed to open dat file');
        }
        try {
            this.outputStream = this.context.contentResolver.openOutputStream(file.uri, 'wt');
            this.outputStreamWriter = new Packages.java.io.OutputStreamWriter(this.outputStream, 'MS932');
            this.bufferedWriter = new Packages.java.io.BufferedWriter(this.outputStreamWriter);
            this.printWriter = new Packages.java.io.PrintWriter(this.outputStreamWriter, true);
        }
        catch (e) {
            this.close();
            throw e;
        }
    };
    DatWriter.prototype.close = function () {
        var _a, _b, _c, _d;
        (_a = this.printWriter) === null || _a === void 0 ? void 0 : _a.close();
        this.printWriter = null;
        (_b = this.bufferedWriter) === null || _b === void 0 ? void 0 : _b.close();
        this.bufferedWriter = null;
        (_c = this.outputStreamWriter) === null || _c === void 0 ? void 0 : _c.close();
        this.outputStreamWriter = null;
        (_d = this.outputStream) === null || _d === void 0 ? void 0 : _d.close();
        this.outputStream = null;
    };
    DatWriter.prototype.write = function (data) {
        this.printWriter.write(data);
    };
    return DatWriter;
}());
