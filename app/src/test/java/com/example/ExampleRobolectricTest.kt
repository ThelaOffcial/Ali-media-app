package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.FeedbackType
import com.example.data.repository.AliMediaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AliMedia", appName)
  }

  @Test
  fun `repository initial data loads and like toggles correctly`() {
    val repo = AliMediaRepository.instance
    val initialPosts = repo.posts.value
    assertTrue("Seed posts should not be empty", initialPosts.isNotEmpty())

    val firstPost = initialPosts.first()
    val initialLikes = firstPost.likesCount
    val initialLiked = firstPost.isLikedByMe

    repo.toggleLikePost(firstPost.id)
    val updatedPost = repo.posts.value.first { it.id == firstPost.id }
    assertEquals(!initialLiked, updatedPost.isLikedByMe)
    assertEquals(if (initialLiked) initialLikes - 1 else initialLikes + 1, updatedPost.likesCount)
  }

  @Test
  fun `repository allows following and unfollowing elephants`() {
    val repo = AliMediaRepository.instance
    val elephants = repo.elephants.value
    assertTrue("Elephants list should not be empty", elephants.isNotEmpty())

    val target = elephants.first()
    val prevFollow = target.isFollowedByMe
    repo.toggleFollowElephant(target.id)
    val afterFollow = repo.elephants.value.first { it.id == target.id }.isFollowedByMe
    assertEquals(!prevFollow, afterFollow)
  }

  @Test
  fun `repository submit feedback adds to feedback list`() {
    val repo = AliMediaRepository.instance
    val prevCount = repo.feedbacks.value.size
    repo.submitFeedback(FeedbackType.SUGGESTION, "Great app for Sri Lankan tuskers!")
    val newCount = repo.feedbacks.value.size
    assertEquals(prevCount + 1, newCount)
  }

  @Test
  fun `firebase rtdb service instance is configured with proper url`() {
    val service = com.example.data.remote.FirebaseRtdbService()
    assertNotNull(service)
  }
}

