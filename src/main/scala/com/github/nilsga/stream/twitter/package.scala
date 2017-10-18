package com.github.nilsga.stream

import com.twitter.hbc.httpclient.auth.OAuth1

package object twitter {

  case class TwitterApiCredentials(consumerKey: String, consumerSecret: String, token: String, tokenSecret: String)

  case class TwitterHbcFilterStreamSettings(credentials: TwitterApiCredentials, trackTerms: Seq[String] = Seq.empty, language: Option[String] = None, gnip: Boolean = false) {
    def withTrackTerms(trackTerms: Seq[String]) = copy(trackTerms = trackTerms)
    def withLanguage(language: String) = copy(language = Some(language))
    def withGnip() = copy(gnip = true)
  }

  implicit def toOauth1Credentials(credentials: TwitterApiCredentials): OAuth1 = {
    new OAuth1(credentials.consumerKey, credentials.consumerSecret, credentials.token, credentials.tokenSecret)
  }

}
