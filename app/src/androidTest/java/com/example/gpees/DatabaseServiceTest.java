package com.example.gpees;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseServiceTest {

    private DatabaseService databaseHelper;

    // A bathroomId we can reuse across tests
    private static String testBathroomId;

    @Before
    public void setUp() {
        databaseHelper = new DatabaseService();
    }

    // Add Bathroom

    @Test
    public void a_testAddBathroom() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        Bathroom bathroom = new Bathroom(
                "Test Bathroom",
                "123 Test St",
                49.8801,
                -119.4436,
                Arrays.asList("SAFE", "ACCESSIBLE")
        );

        databaseHelper.addBathroom(bathroom, new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                testBathroomId = bathroom.getId();
                assertNotNull("Bathroom ID should not be null after save", testBathroomId);
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("addBathroom failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Get Bathrooms

    @Test
    public void b_testGetBathrooms() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        databaseHelper.getBathrooms(new DatabaseService.BathroomsCallback() {
            @Override
            public void onSuccess(List<Bathroom> bathrooms) {
                assertNotNull("Bathrooms list should not be null", bathrooms);
                assertFalse("Bathrooms list should not be empty", bathrooms.isEmpty());
                for (Bathroom b : bathrooms) {
                    assertNotNull("Each bathroom should have an ID", b.getId());
                    assertNotNull("Each bathroom should have a name", b.getName());
                }
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getBathrooms failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Get Bathrooms In Range

    @Test
    public void c_testGetBathroomsInRange() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        // Range around the test bathroom coordinates
        double minLat = 49.0;
        double maxLat = 50.0;
        double minLng = -120.0;
        double maxLng = -119.0;

        databaseHelper.getBathroomsInRange(minLat, maxLat, minLng, maxLng, new DatabaseService.BathroomsCallback() {
            @Override
            public void onSuccess(List<Bathroom> bathrooms) {
                assertNotNull("Result should not be null", bathrooms);
                for (Bathroom b : bathrooms) {
                    // Verify every returned bathroom is actually within bounds
                    assertTrue("Latitude should be in range",
                            b.getLatitude() >= minLat && b.getLatitude() <= maxLat);
                    assertTrue("Longitude should be in range",
                            b.getLongitude() >= minLng && b.getLongitude() <= maxLng);
                }
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getBathroomsInRange failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Add Review

    @Test
    public void d_testAddReview() {
        // Requires testAddBathroom to have run first to populate testBathroomId
        assertNotNull("testBathroomId must be set before running this test", testBathroomId);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        Review review = new Review("testuser", 4.0f, "Pretty clean!", "2025-08-01");

        databaseHelper.addReview(testBathroomId, review, new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("addReview failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Get Reviews

    @Test
    public void e_testGetReviews() {
        assertNotNull("testBathroomId must be set before running this test", testBathroomId);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        databaseHelper.getReviews(testBathroomId, new DatabaseService.ReviewsCallback() {
            @Override
            public void onSuccess(List<Review> reviews) {
                assertNotNull("Reviews list should not be null", reviews);
                assertFalse("Reviews list should not be empty", reviews.isEmpty());
                for (Review r : reviews) {
                    assertNotNull("Each review should have a username", r.getUsername());
                    assertTrue("Rating should be between 0 and 5",
                            r.getRating() >= 0 && r.getRating() <= 5);
                }
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getReviews failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Add Comment

    @Test
    public void f_testAddComment() {
        // Requires testAddBathroom to have run first to populate testBathroomId
        assertNotNull("testBathroomId must be set before running this test", testBathroomId);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        Comment comment = new Comment("testuser", "don't use the last stall", "2025-08-01");

        databaseHelper.addComment(testBathroomId, comment, new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("addComment failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Get Comments

    @Test
    public void g_testGetComments() {
        assertNotNull("testBathroomId must be set before running this test", testBathroomId);

        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        databaseHelper.getComments(testBathroomId, new DatabaseService.CommentsCallback() {
            @Override
            public void onSuccess(List<Comment> comments) {
                assertNotNull("Comments list should not be null", comments);
                assertFalse("Comments list should not be empty", comments.isEmpty());
                for (Comment c : comments) {
                    assertNotNull("Each comment should have a username", c.getUsername());
                    assertFalse("Comment should not be empty",
                            c.getComment().isEmpty());
                }
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getComments failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Helper

    private void awaitLatch(CountDownLatch latch) {
        try {
            // Wait up to 10 seconds for the Firebase call to complete
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) {
                fail("Test timed out waiting for Firebase response");
            }
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}
