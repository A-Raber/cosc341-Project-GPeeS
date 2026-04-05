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
        List<Bathroom> seedData = Arrays.asList(
            new Bathroom("City Park Washroom", "1600 Abbott St", 49.8841, -119.4978, Arrays.asList("accessible", "safe")),
            new Bathroom("Waterfront Park", "1200 Water St", 49.8925, -119.4975, Arrays.asList("safe")),
            new Bathroom("Downtown Paid Toilet", "Bernard Ave", 49.8872, -119.4961, Arrays.asList("cost", "accessible")),
            new Bathroom("Gyro Beach Washroom", "3400 Lakeshore Rd", 49.8520, -119.4895, Arrays.asList("accessible", "clean")),
            new Bathroom("Orchard Park Mall", "2271 Harvey Ave", 49.8828, -119.4428, Arrays.asList("safe", "clean"))
        );

        for (Bathroom bathroom : seedData) {
            CountDownLatch latch = new CountDownLatch(1);
            databaseHelper.addBathroom(bathroom, new DatabaseService.WriteCallback() {
                @Override
                public void onSuccess() { latch.countDown(); }
                @Override
                public void onFailure(Exception e) { latch.countDown(); }
            });
            awaitLatch(latch);
        }
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
