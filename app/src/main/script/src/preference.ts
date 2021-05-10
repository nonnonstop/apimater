/**
 * Callback of creating preference at MainActivity
 * 
 * This function is called from MainActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function onCreateDatPreference(fragment: Fragment, preference: Preference): ActivityResultLauncher {
    const context = fragment.getContext().getApplicationContext();

    const contracts = new Packages.androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree();
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const resultCallback = (uri: any) => {
        if (uri === null) {
            return;
        }
        context.getContentResolver().takePersistableUriPermission(uri, 3);

        const prefEditor = context.getSharedPreferences('DatWriter', 0).edit();
        prefEditor.putString('documentTreeUri', uri);
        prefEditor.commit();
    }
    return fragment.registerForActivityResult(contracts, resultCallback);
}

/**
 * Callback of clicking preference at MainActivity
 * 
 * This function is called from MainActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function onClickDatPreference(fragment: Fragment, launcher: ActivityResultLauncher): void {
    const documentUri = Packages.android.provider.DocumentsContract.buildDocumentUri(
        'com.android.externalstorage.documents',
        'primary:Android/data/jp.co.airfront.android.a2chMate/files/2chMate/dat');
    launcher.launch(documentUri);
}
