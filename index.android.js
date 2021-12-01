// import { NativeModules } from 'react-native'

// const { JitsiMeetModule } = NativeModules


// export default JitsiMeetModule
/**
 * @providesModule JitsiMeet
 */

 import { NativeModules, requireNativeComponent } from 'react-native';

 export const JitsiMeetView = requireNativeComponent('RNJitsiMeetView');
 export const JitsiMeetModule = NativeModules.RNJitsiMeetModule
 const call = JitsiMeetModule.call;
 const audioCall = JitsiMeetModule.audioCall;
 const endCall = JitsiMeetModule.endCall;

 JitsiMeetModule.call = (url, userInfo) => {
   userInfo = userInfo || {};
   call(url, userInfo);
 }
 JitsiMeetModule.audioCall = (url, userInfo) => {
   userInfo = userInfo || {};
   audioCall(url, userInfo);
 }

 JitsiMeetModule.endCall = () => {
  endCall();
}
 export default JitsiMeetModule;
 
