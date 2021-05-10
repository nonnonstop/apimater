"use strict";
/**
 * Callback of creating preference at MainActivity
 *
 * This function is called from MainActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function onCreateDatPreference(fragment, preference) {
    var context = fragment.getContext().getApplicationContext();
    var contracts = new Packages.androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    var resultCallback = function (uri) {
        if (uri === null) {
            return;
        }
        context.getContentResolver().takePersistableUriPermission(uri, 3);
        var prefEditor = context.getSharedPreferences('DatWriter', 0).edit();
        prefEditor.putString('documentTreeUri', uri);
        prefEditor.commit();
    };
    return fragment.registerForActivityResult(contracts, resultCallback);
}
/**
 * Callback of clicking preference at MainActivity
 *
 * This function is called from MainActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function onClickDatPreference(fragment, launcher) {
    var documentUri = Packages.android.provider.DocumentsContract.buildDocumentUri('com.android.externalstorage.documents', 'primary:Android/data/jp.co.airfront.android.a2chMate/files/2chMate/dat');
    launcher.launch(documentUri);
}
