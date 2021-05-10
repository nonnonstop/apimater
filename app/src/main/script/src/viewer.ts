/**
 * Open viewer
 * 
 * This function is called from LoadActivity on UI thread.
 */
// eslint-disable-next-line @typescript-eslint/no-unused-vars
function view(activity: Activity, htmlUrl: string) {
    activity.getSystemService('activity')
        .killBackgroundProcesses('jp.co.airfront.android.a2chMate');
    activity.finish();
}
