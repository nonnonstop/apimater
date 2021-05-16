/**
 * Translate to DAT file
 * 
 * This function is called from LoadActivity on worker thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function translate(context: ApplicationContext, htmlUrl: string): void {
    const datWriter = new DatWriter(context);
    const translators: Translator[] = [
        new HtmlToDatTranslator(htmlUrl, datWriter),
        new ApiToDatTranslator(htmlUrl, datWriter),
        new ScToNetTranslator(htmlUrl, datWriter),
    ];
    for (const translator of translators) {
        try {
            if (translator.translate())
                return;
        } catch (e) {
            Packages.timber.log.Timber.e(
                Packages.org.mozilla.javascript.EvaluatorException(e.toString()),
                'Failed to translate');
        }
    }
    throw Error('Failed to translate');
}

/**
 * Base class of dat translator
 */
abstract class Translator {
    protected htmlUrl: string;
    protected server: string;
    protected board: string;
    protected thread: string;
    private datFilename: string;
    private writer: DatWriter

    public constructor(htmlUrl: string, writer: DatWriter) {
        const urlMatcher = htmlUrl.match(/^https?:\/\/(\w+)\.[\w.]+\/test\/read\.(?:cgi|php)\/(\w+)\/(\d+)/);
        if (urlMatcher === null) {
            throw new Error('Unexpected URL');
        }
        this.htmlUrl = urlMatcher[0];
        this.server = urlMatcher[1];
        this.board = urlMatcher[2];
        this.thread = urlMatcher[3];
        this.datFilename = `${this.board}_${this.thread}.dat`;
        this.writer = writer;
    }

    protected openDat(): void {
        this.writer.open(this.datFilename);
    }

    protected writeDat(str: string): void {
        this.writer.write(str);
    }

    protected closeDat(): void {
        this.writer.close();
    }

    public abstract translate(): boolean;
}

/**
 * Dat translator using 5ch html
 */
class HtmlToDatTranslator extends Translator {
    private requestHtml(): string {
        const client = new Packages.okhttp3.OkHttpClient();
        const request = new Packages.okhttp3.Request.Builder()
            .url(this.htmlUrl)
            .build();
        const responseCharset = Packages.java.nio.charset.Charset.forName('MS932');
        const response = client.newCall(request).execute();
        try {
            if (response.code() !== 200) {
                throw new Error('Failed to connect');
            }
            const body = response.body().source().readString(responseCharset);
            return body + ''; // convert Java String to JS String
        } finally {
            response.close();
        }
    }

    private convertToDat(htmlData: string): void {
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
    }

    private convertToDatV5(htmlData: string): void {
        let title: string | null = (htmlData.match(/<title>(.*?) ?<\/title>/)?.[1] ?? '') + '\t';
        const resRegexp = /<dt>(\d+) ：(?:<a href="mailto:([^"]*)">(.*?)<\/a>|<font color=green>(.*?)<\/font>)：(.*?)<dd>(.*)<br><br>/g;
        try {
            for (; ;) {
                const res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                const mail = res[2] ?? '';
                const name = (res[3] ?? res[4])
                    .replace(/^<b>(.*)<\/b>$/g, '$1');
                const date = res[5]
                    .replace(/<a href="javascript:be\((\d+)\);">\?([^<]+)<\/a>/g, 'BE:$1-$2');
                const message = res[6]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="http(:\/\/[^"]+)">/g, 'sssp$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>${title}\n`);
                    title = null;
                }
                else {
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>\n`);
                }
            }
            if (title !== null)
                throw new Error('Res not found');
        }
        finally {
            this.closeDat();
        }
    }

    private convertToDatV6(htmlData: string): void {
        let title: string | null = (htmlData.match(/<title>(.*?)\n<\/title>/)?.[1] ?? '') + '\t';
        const resRegexp = /<div class="post"[^>]*><div class="number">(?!0)(\d+) : <\/div><div class="name"><b>(?:<a href="mailto:([^"]*)">)?(.*?)(?:<\/a>)?<\/b><\/div><div class="date">(.*?)<\/div><div class="message">(.*?)<\/div><\/div>/g;
        try {
            for (; ;) {
                const res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                const mail = res[2] ?? '';
                const name = res[3];
                const date = res[4]
                    .replace(/<\/div><div class="be [^"]+"><a href="http:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2');
                const message = res[5]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="(\/\/[^"]+)">/g, 'sssp:$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>${title}\n`);
                    title = null;
                }
                else {
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>\n`);
                }
            }
            if (title !== null)
                throw new Error('Res not found');
        }
        finally {
            this.closeDat();
        }
    }

    private convertToDatV7(htmlData: string): void {
        let title: string | null = (htmlData.match(/<title>(.*?)\n<\/title>/)?.[1] ?? '') + '\t';
        const resRegexp = /<div class="post"[^>]*><div class="meta"><span class="number">0*(\d+)<\/span><span class="name"><b>(?:<a href="mailto:([^"]*)">)?(.*?)(?:<\/a>)?<\/b><\/span><span class="date">(.*?)<\/span><span class="uid">(.*?)<\/span><\/div><div class="message"><span class="escaped">(.*?)<\/span><\/div><\/div>/g;
        try {
            for (; ;) {
                const res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                const mail = res[2] ?? '';
                const name = res[3];
                const date = res[4] + ' ' + res[5]
                    .replace(/<\/span><span class="be [^"]+"><a href="http:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2');
                const message = res[6]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="(\/\/[^"]+)">/g, 'sssp:$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>${title}\n`);
                    title = null;
                }
                else {
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>\n`);
                }
            }
            if (title !== null)
                throw new Error('Res not found');
        }
        finally {
            this.closeDat();
        }
    }

    private convertToDatPink(htmlData: string): void {
        let title: string | null = htmlData.match(/<title>(.*?)\n<\/title>/)?.[1] ?? '';
        const resRegexp = /<dl class="post"[^>]*><dt class=""><span class="number">(?!0)(\d+) : <\/span><span class="name"><b>(?:<a href="mailto:([^"]*)">(.*?)<\/a>|<font color="green">(.*?)<\/font>)<\/b><\/span><span class="date">(.*?)<\/(?:span|div)><\/dt><dd class="thread_in">(.*?)<\/dd><\/dl>/g;
        try {
            for (; ;) {
                const res = resRegexp.exec(htmlData);
                if (res === null)
                    break;
                const mail = res[2] ?? '';
                const name = res[3] ?? res[4];
                const date = res[5]
                    .replace(/<\/span><div class="be [^"]+"><a href="https:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2');
                const message = res[6]
                    .replace(/<a [^>]*>(https?:\/\/[^<]*)<\/a>/g, '$1')
                    .replace(/(<br> )(?=<br>)/g, '$1 ')
                    .replace(/<img src="http(:\/\/[^"]+)">/g, 'sssp$1');
                if (title !== null) {
                    this.openDat();
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>${title}\n`);
                    title = null;
                }
                else {
                    this.writeDat(`${name}<>${mail}<>${date}<>${message}<>\n`);
                }
            }
            if (title !== null)
                throw new Error('Res not found');
        }
        finally {
            this.closeDat();
        }
    }

    public translate(): boolean {
        const htmlData = this.requestHtml();
        this.convertToDat(htmlData);
        return true;
    }
}

/**
 * Dat translator using 2ch.sc dat
 */
class ScToNetTranslator extends Translator {
    private requestScDat(): string {
        const urlMatcher = this.htmlUrl.match(/^https?:\/\/(\w+)\.[\w.]+\/test\/read\.(?:cgi|php)\/(\w+)\/(\d+)/);
        if (urlMatcher === null) {
            throw new Error('Unexpected URL');
        }
        const datUrl = `http://${urlMatcher[1]}.2ch.sc/${urlMatcher[2]}/dat/${urlMatcher[3]}.dat`;
        const client = new Packages.okhttp3.OkHttpClient();
        const request = new Packages.okhttp3.Request.Builder()
            .url(datUrl)
            .build();
        const responseCharset = Packages.java.nio.charset.Charset.forName('MS932');
        const response = client.newCall(request).execute();
        try {
            if (response.code() !== 200) {
                throw new Error('Failed to connect');
            }
            const body = response.body().source().readString(responseCharset);
            return body + ''; // convert Java String to JS String
        } finally {
            response.close();
        }
    }

    private convertToNetDat(scDat: string): void {
        try {
            let found = false;
            const datLines = scDat.split(/\r\n|\n|\r/);
            for (const datLine of datLines) {
                const res = datLine.split('<>')
                if (res.length < 4)
                    break
                res[0] = res[0]
                    .replace(/アフィサイトへの＼\(\^o\^\)／です/g, 'アフィサイトへの転載は禁止です')
                    .replace(/＠＼\(\^o\^\)／/g, '＠無断転載禁止');
                res[2] = res[2]
                    .replace(/<a href="http:\/\/be\.[25]ch\.net\/user\/(\d+)"[^>]*>\?([^<]+)<\/a>/g, 'BE:$1-$2')
                    .replace(/( ID:[^ .]+)($| )/g, '$1.sc$2')
                    .replace(/(\.\d+)($| (?!ID:)(?!.*\.net))/g, '$1 .sc$2')
                    .replace(/\.net/g, '');
                res[3] = res[3]
                    .replace(/sssp:\/\/img\.2ch\.sc\/ico\//g, 'sssp://img.5ch.sc/ico/')
                    .replace(/<img src="(\/\/[^"]+)">/g, 'sssp:$1');
                if (!found) {
                    this.openDat();
                    found = true;
                }
                this.writeDat(res.join('<>') + '\n');
            }
            if (!found)
                throw new Error('Res not found');
        }
        finally {
            this.closeDat();
        }
    }

    public translate(): boolean {
        const scDat = this.requestScDat();
        this.convertToNetDat(scDat);
        return true;
    }
}

/**
 * Dat translator using API
 */
class ApiToDatTranslator extends Translator {
    private static readonly APP_KEY = '<2CH APP KEY>';
    private static readonly HM_KEY = '<2CH HM KEY>';
    private static readonly SID = 'AbCdEfgHijklMnopqrsTuvwxyz';

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private mac: any;

    public constructor(htmlUrl: string, writer: DatWriter) {
        super(htmlUrl, writer);
        const hmkey = ApiToDatTranslator.toBytes(ApiToDatTranslator.HM_KEY);
        const secret = new Packages.javax.crypto.spec.SecretKeySpec(hmkey, 'HmacSHA256');
        this.mac = Packages.javax.crypto.Mac.getInstance('HmacSHA256');
        this.mac.init(secret);
    }

    private request(path: string): string {
        const message = ApiToDatTranslator.toBytes(path + ApiToDatTranslator.SID + ApiToDatTranslator.APP_KEY);
        const hobo = Packages.java.lang.String.format('%064x', new Packages.java.math.BigInteger(1, this.mac.doFinal(message)))

        const client = new Packages.okhttp3.OkHttpClient();
        const body = new Packages.okhttp3.FormBody.Builder()
            .add('sid', ApiToDatTranslator.SID)
            .add('hobo', hobo)
            .add('appkey', ApiToDatTranslator.APP_KEY)
            .build();
        const request = new Packages.okhttp3.Request.Builder()
            .url('https://kg.dev5ch.net' + path)
            .addHeader('User-Agent', 'Monazilla/1.00 2chMate/0.8.10.89 Dalvik/2.1.0 (Linux; U; Android 10; Pixel 3a Build/QQ2A.200305.002)')
            .addHeader('X-2ch-UA', '2chMate/0.8.10.89')
            .post(body)
            .build();
        const responseCharset = Packages.java.nio.charset.Charset.forName('MS932');
        const response = client.newCall(request).execute();
        try {
            if (response.code() !== 200) {
                throw new Error('Failed to connect');
            }
            const body = response.body().source().readString(responseCharset);
            return body + ''; // convert Java String to JS String
        } finally {
            response.close();
        }
    }

    private static toBytes(str: string): number[] {
        const bytes: number[] = [];
        const length = str.length;
        for (let i = 0; i < length; ++i) {
            const code = str.charCodeAt(i);
            bytes.push(code);
        }
        return bytes;
    }

    public translate(): boolean {
        if (ApiToDatTranslator.APP_KEY.length !== 30)
            return false;
        if (ApiToDatTranslator.HM_KEY.length !== 30)
            return false;
        const id = JSON.parse(this.request(`/api/v1/prepare/${this.server}/${this.board}/${this.thread}`)).id;
        Packages.java.lang.Thread.sleep(5000);
        try {
            const dat = this.request(`/api/v1/get/${id}`);
            this.openDat();
            this.writeDat(dat);
            return true;
        }
        finally {
            this.closeDat();
        }
    }
}

/**
 * Dat writer
 */
class DatWriter {
    private context: ApplicationContext;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private outputStream: any;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private outputStreamWriter: any;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private bufferedWriter: any;
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    private printWriter: any;

    public constructor(context: ApplicationContext) {
        this.context = context;
    }

    public open(filename: string): void {
        const baseUriStr = this.context.getSharedPreferences('DatWriter', 0).getString('documentTreeUri', null);
        if (baseUriStr === null) {
            throw new Error('Dat directory is not initialized');
        }
        const baseUri = Packages.android.net.Uri.parse(baseUriStr);
        const baseDir = Packages.androidx.documentfile.provider.DocumentFile.fromTreeUri(this.context, baseUri);
        if (baseDir === null) {
            throw new Error('Failed to open dat directory');
        }
        const file = baseDir.findFile(filename) ?? baseDir.createFile('application/octet-stream', filename);
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
    }

    public close() {
        this.printWriter?.close();
        this.printWriter = null;
        this.bufferedWriter?.close();
        this.bufferedWriter = null;
        this.outputStreamWriter?.close();
        this.outputStreamWriter = null;
        this.outputStream?.close();
        this.outputStream = null;
    }

    public write(data: string) {
        this.printWriter.write(data);
    }
}
