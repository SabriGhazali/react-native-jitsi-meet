#import "RNJitsiMeetViewManager.h"
#import "RNJitsiMeetView.h"
#import <JitsiMeetSDK/JitsiMeetUserInfo.h>

@implementation RNJitsiMeetViewManager{
    RNJitsiMeetView *jitsiMeetView;
}

RCT_EXPORT_MODULE(RNJitsiMeetView)
RCT_EXPORT_VIEW_PROPERTY(onConferenceJoined, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onConferenceTerminated, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onParticipantLeft, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onParticipantJoined, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onConferenceWillJoin, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onEnteredPip, RCTBubblingEventBlock)

- (UIView *)view
{
  jitsiMeetView = [[RNJitsiMeetView alloc] init];
  jitsiMeetView.delegate = self;
  return jitsiMeetView;
}

RCT_EXPORT_METHOD(initialize)
{
    RCTLogInfo(@"Initialize is deprecated in v2");
}

RCT_EXPORT_METHOD(call:(NSString *)urlString userInfo:(NSDictionary *)userInfo)
{
    RCTLogInfo(@"Load URL %@", urlString);
    JitsiMeetUserInfo * _userInfo = [[JitsiMeetUserInfo alloc] init];
    if (userInfo != NULL) {
      if (userInfo[@"displayName"] != NULL) {
        _userInfo.displayName = userInfo[@"displayName"];
      }
      if (userInfo[@"email"] != NULL) {
        _userInfo.email = userInfo[@"email"];
      }
      if (userInfo[@"avatar"] != NULL) {
        NSURL *url = [NSURL URLWithString:[userInfo[@"avatar"] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
        _userInfo.avatar = url;
      }
    }
    dispatch_sync(dispatch_get_main_queue(), ^{
        JitsiMeetConferenceOptions *options = [JitsiMeetConferenceOptions fromBuilder:^(JitsiMeetConferenceOptionsBuilder *builder) {        
            builder.room = urlString;
            builder.userInfo = _userInfo;
            [builder setFeatureFlag:@"pip.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"invite.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"meeting-name.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"conference-timer.enabled" withBoolean:NO];

        }];
        [jitsiMeetView join:options];
    });
}

RCT_EXPORT_METHOD(audioCall:(NSString *)urlString userInfo:(NSDictionary *)userInfo)
{
    RCTLogInfo(@"Load Audio only URL %@", urlString);
    JitsiMeetUserInfo * _userInfo = [[JitsiMeetUserInfo alloc] init];
    if (userInfo != NULL) {
      if (userInfo[@"displayName"] != NULL) {
        _userInfo.displayName = userInfo[@"displayName"];
      }
      if (userInfo[@"email"] != NULL) {
        _userInfo.email = userInfo[@"email"];
      }
      if (userInfo[@"avatar"] != NULL) {
        NSURL *url = [NSURL URLWithString:[userInfo[@"avatar"] stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
        _userInfo.avatar = url;
      }
    }
    dispatch_sync(dispatch_get_main_queue(), ^{
        JitsiMeetConferenceOptions *options = [JitsiMeetConferenceOptions fromBuilder:^(JitsiMeetConferenceOptionsBuilder *builder) {        
            builder.room = urlString;
            builder.userInfo = _userInfo;
            builder.audioOnly = YES;

            [builder setFeatureFlag:@"pip.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"invite.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"meeting-name.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"conference-timer.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"replace.participant" withBoolean:YES];
            [builder setFeatureFlag:@"filmstrip.enabled" withBoolean:NO];
            [builder setFeatureFlag:@"toolbox.alwaysVisible" withBoolean:YES];

            

        }];
        [jitsiMeetView join:options];
    });
}

RCT_EXPORT_METHOD(endCall)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [jitsiMeetView hangUp];        
        NSLog(@"++ endCall = ");

    });
}

RCT_EXPORT_METHOD(retrieveInfos:(RCTResponseSenderBlock)callback)
{
    dispatch_sync(dispatch_get_main_queue(), ^{
        [jitsiMeetView retrieveParticipantsInfo:^(NSArray * _Nullable arrayInfo) {
            
         //   RCTLogInfo(@"retrieveParticipantsInfo %@",arrayInfo);

            NSLog(@"++ retrieveParticipantsInfo = %@",arrayInfo);
            
            callback(@[[NSNull null], arrayInfo]);

        }];
    });
}

#pragma mark JitsiMeetViewDelegate

- (void)conferenceJoined:(NSDictionary *)data {
    RCTLogInfo(@"Conference joined");
    if (!jitsiMeetView.onConferenceJoined) {
        return;
    }

    jitsiMeetView.onConferenceJoined(data);
}

- (void)conferenceTerminated:(NSDictionary *)data {
    RCTLogInfo(@"Conference terminated");
    if (!jitsiMeetView.onConferenceTerminated) {
        return;
    }

    jitsiMeetView.onConferenceTerminated(data);
}


- (void)conferenceWillJoin:(NSDictionary *)data {
    RCTLogInfo(@"Conference will join");
    if (!jitsiMeetView.onConferenceWillJoin) {
        return;
    }

    jitsiMeetView.onConferenceWillJoin(data);
}

- (void)participantLeft:(NSDictionary *)data {
    // RCTLogInfo(@"participantLeft");
    if (!jitsiMeetView.onParticipantLeft) {
        return;
    }
    
    RCTLogInfo(@"participantLeft %@",data);

    jitsiMeetView.onParticipantLeft(data);
}

- (void)enterPictureInPicture:(NSDictionary *)data {
    RCTLogInfo(@"Enter Picture in Picture");
    if (!jitsiMeetView.onEnteredPip) {
        return;
    }

    jitsiMeetView.onEnteredPip(data);
}

- (void)participantJoined:(NSDictionary *)data {
     RCTLogInfo(@"onParticipantJoined");

    if (!jitsiMeetView.onParticipantJoined) {
        return;
    }
    
    RCTLogInfo(@"onParticipantJoined %@",data);

     jitsiMeetView.onParticipantJoined(data);
}


@end
