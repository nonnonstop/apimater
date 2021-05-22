/**
 * Translate to DAT file
 * 
 * This function is called from LoadActivity on worker thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function translate(context: ApplicationContext, fullUrl: string): void {
    const builtin = Packages.com.nonnonstop.apimate.translator;
    const info = builtin.BuiltinDatInfo(fullUrl);
    const writer = builtin.BuiltinDatWriter(context);
    const translators = [
        builtin.BuiltinHtmlTranslator(),
        builtin.BuiltinApiTranslator(
            '<5CH HM KEY>',
            '<5CH APP KEY>',
            'Monazilla/1.00 2chMate/0.8.10.89 Dalvik/2.1.0 (Linux; U; Android 10; Pixel 3a Build/QQ2A.200305.002)',
            '2chMate/0.8.10.89',
            'AbCdEfgHijklMnopqrsTuvwxyz',
            5000),
        builtin.BuiltinScTranslator(),
    ];
    try {
        writer.open(info);
        for (const translator of translators) {
            try {
                if (translator.translate(info, writer))
                    return;
            } catch (e) {
                Packages.timber.log.Timber.e(
                    Packages.org.mozilla.javascript.EvaluatorException(e.toString()),
                    'Failed to translate');
            }
        }
    } finally {
        writer.close();
    }
    throw Error('Failed to translate');
}
