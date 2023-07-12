"use strict";
/**
 * Translate to DAT file
 *
 * This function is called from LoadActivity on worker thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function translate(context, fullUrl) {
    var builtin = Packages.com.nonnonstop.apimate.translator;
    var info = builtin.BuiltinDatInfo(fullUrl);
    var writer = builtin.BuiltinDatWriter(context);
    var translators = [
        builtin.BuiltinApiTranslator('<5CH HM KEY>', '<5CH APP KEY>', 'Monazilla/1.00 2chMate/0.8.10.89 Dalvik/2.1.0 (Linux; U; Android 10; Pixel 3a Build/QQ2A.200305.002)', '2chMate/0.8.10.89', 'AbCdEfgHijklMnopqrsTuvwxyz', 5000),
        builtin.BuiltinItestTranslator(),
        builtin.BuiltinHtmlTranslator(),
        builtin.BuiltinScTranslator(),
    ];
    try {
        writer.open(info);
        for (var _i = 0, translators_1 = translators; _i < translators_1.length; _i++) {
            var translator = translators_1[_i];
            try {
                if (translator.translate(info, writer))
                    return;
            }
            catch (e) {
                var javaException = void 0;
                if (e.javaException)
                    javaException = e.javaException;
                else if (e.rhinoException)
                    javaException = e.rhinoException;
                else
                    javaException = Packages.org.mozilla.javascript.EvaluatorException(e.toString());
                Packages.timber.log.Timber.e(javaException, 'Failed to translate');
            }
        }
    }
    finally {
        writer.close();
    }
    throw Error('Failed to translate');
}
