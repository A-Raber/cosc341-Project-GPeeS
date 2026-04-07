package com.example.gpees;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DatabaseServiceTest {

    private interface SeedCallback {
        void onSuccess();
        void onFailure(String message);
    }

    private static class SeedBathroomData {
        private final Bathroom bathroom;
        private final List<Review> reviews;
        private final List<Comment> comments;

        private SeedBathroomData(Bathroom bathroom, List<Review> reviews, List<Comment> comments) {
            this.bathroom = bathroom;
            this.reviews = reviews;
            this.comments = comments;
        }
    }

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
                Arrays.asList("accessible")
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

    // Get Bathrooms Nearby
    @Test
    public void c_testGetBathroomsNearby() {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        // Search around Kelowna downtown
        double lat = 49.888;
        double lng = -119.496;
        double radius = 5000; // 5km

        databaseHelper.getBathroomsNearby(lat, lng, radius, new DatabaseService.BathroomsCallback() {
            @Override
            public void onSuccess(List<Bathroom> bathrooms) {
                assertNotNull("Result should not be null", bathrooms);
                // We expect at least the one added in test A or seeded ones
                passed[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("getBathroomsNearby failed: " + e.getMessage());
                latch.countDown();
            }
        });

        awaitLatch(latch);
        assertTrue(passed[0]);
    }

    // Add Review
    @Test
    public void d_testAddReview() {
        assertNotNull("testBathroomId must be set before running this test", testBathroomId);
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] passed = {false};

        Review review = new Review("testuser", 4.0f, "Pretty clean!", new Date());

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

    // Seed Kelowna Data (Run this to populate your map for testing)
    @Test
    public void z_seedKelownaBathrooms() {
        List<SeedBathroomData> seedData = Arrays.asList(
            new SeedBathroomData(
                new Bathroom("City Park Washroom", "1600 Abbott St", 49.8841, -119.4978, Arrays.asList("accessible", "safe")),
                Arrays.asList(
                    new Review("kelownalocal", 4.5f, "Usually stocked and close to the beach.", new Date(1743206400000L)),
                    new Review("morningwalker", 4.0f, "Clean early in the day.", new Date(1743724800000L))
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Best option when you're already in City Park.", new Date(1743811200000L)),
                    new Comment("beachrunner", "Line gets longer in the afternoon.", new Date(1743897600000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Waterfront Park", "1200 Water St", 49.8925, -119.4975, Arrays.asList("safe")),
                Arrays.asList(
                    new Review("tourist22", 3.5f, "Convenient but can get busy on weekends.", new Date(1743033600000L)),
                    new Review("lakeview", 4.0f, "Good stop while walking the boardwalk.", new Date(1743552000000L))
                ),
                Arrays.asList(
                    new Comment("dockside", "Lighting is decent after sunset.", new Date(1743638400000L)),
                    new Comment("Anonymous", "Bring your own sanitizer just in case.", new Date(1743984000000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Downtown Paid Toilet", "Bernard Ave", 49.8872, -119.4961, Arrays.asList("cost", "accessible")),
                Arrays.asList(
                    new Review("budgettraveler", 2.5f, "Fine in an emergency, but paying is annoying.", new Date(1742860800000L)),
                    new Review("wheelsonroad", 4.0f, "Easy accessible entry and enough space.", new Date(1743379200000L))
                ),
                Arrays.asList(
                    new Comment("downtowncommuter", "Card reader worked for me.", new Date(1743465600000L)),
                    new Comment("Anonymous", "Has been cleaner lately.", new Date(1744070400000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Gyro Beach Washroom", "3400 Lakeshore Rd", 49.8520, -119.4895, Arrays.asList("accessible", "clean")),
                Arrays.asList(
                    new Review("sunsetswim", 5.0f, "Surprisingly clean for a beach washroom.", new Date(1742774400000L)),
                    new Review("familyday", 4.5f, "Spacious and easy to find.", new Date(1743292800000L))
                ),
                Arrays.asList(
                    new Comment("parentmode", "Good stop if you have kids with you.", new Date(1743552000000L)),
                    new Comment("lakeshorelocal", "Closed briefly one morning for cleaning.", new Date(1744156800000L))
                )
            ),
            new SeedBathroomData(
                new Bathroom("Orchard Park Mall", "2271 Harvey Ave", 49.8828, -119.4428, Arrays.asList("safe", "clean")),
                Arrays.asList(
                    new Review("shopbreak", 4.5f, "Reliable and usually very clean.", new Date(1742947200000L)),
                    new Review("mallhopper", 4.0f, "Easy to access during store hours.", new Date(1743465600000L))
                ),
                Arrays.asList(
                    new Comment("Anonymous", "Near the food court entrance.", new Date(1743724800000L)),
                    new Comment("weekenderrand", "Busy at lunch but still manageable.", new Date(1744243200000L))
                )
            )
        );

        for (SeedBathroomData seedBathroom : seedData) {
            CountDownLatch latch = new CountDownLatch(1);
            final String[] errorMessage = {null};

            databaseHelper.addBathroom(seedBathroom.bathroom, new DatabaseService.WriteCallback() {
                @Override
                public void onSuccess() {
                    seedReviewsThenComments(
                            seedBathroom.bathroom.getId(),
                            seedBathroom.reviews,
                            seedBathroom.comments,
                            new SeedCallback() {
                                @Override
                                public void onSuccess() {
                                    latch.countDown();
                                }

                                @Override
                                public void onFailure(String message) {
                                    errorMessage[0] = message;
                                    latch.countDown();
                                }
                            }
                    );
                }

                @Override
                public void onFailure(Exception e) {
                    errorMessage[0] = "addBathroom seed failed: " + e.getMessage();
                    latch.countDown();
                }
            });

            awaitLatch(latch);

            if (errorMessage[0] != null) {
                fail(errorMessage[0]);
            }
        }
    }

    private void seedReviewsThenComments(String bathroomId, List<Review> reviews, List<Comment> comments, SeedCallback callback) {
        seedReviewAtIndex(bathroomId, reviews, 0, new SeedCallback() {
            @Override
            public void onSuccess() {
                seedCommentAtIndex(bathroomId, comments, 0, callback);
            }

            @Override
            public void onFailure(String message) {
                callback.onFailure(message);
            }
        });
    }

    private void seedReviewAtIndex(String bathroomId, List<Review> reviews, int index, SeedCallback callback) {
        if (index >= reviews.size()) {
            callback.onSuccess();
            return;
        }

        databaseHelper.addReview(bathroomId, reviews.get(index), new DatabaseService.WriteCallback() {
            @Override
            public void onSuccess() {
                seedReviewAtIndex(bathroomId, reviews, index + 1, callback);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure("addReview seed failed: " + e.getMessage());
            }
        });
    }

    private void seedCommentAtIndex(String bathroomId, List<Comment> comments, int index, SeedCallback callback) {
        if (index >= comments.size()) {
            callback.onSuccess();
            return;
        }

        databaseHelper.addComment(bathroomId, comments.get(index), new DatabaseService.WriteCallback() {
                @Override
                public void onSuccess() {
                    seedCommentAtIndex(bathroomId, comments, index + 1, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure("addComment seed failed: " + e.getMessage());
                }
            });
    }

    private void awaitLatch(CountDownLatch latch) {
        try {
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) { fail("Test timed out waiting for Firebase response"); }
        } catch (InterruptedException e) {
            fail("Test interrupted: " + e.getMessage());
        }
    }
}
