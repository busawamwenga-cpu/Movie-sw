const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");
const { serverTimestamp } = require("firebase/firestore");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const DATABASE_ID = "ai-studio-android-moviesw-3661e8be-1922-4c76-ba5d-052e08a10ac7";
const USER_UID = "user_regular_1";
const OTHER_UID = "user_other_2";
const ADMIN_UID = "user_admin_1";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

async function seedAdminDoc(uid) {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore({ databaseId: DATABASE_ID });
    await db.collection("users").doc(uid).set({
      uid,
      email: "admin@moviesw.com",
      displayName: "Admin User",
      role: "admin",
      appVersionCode: 1,
      isLocked: false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
  });
}

async function seedUserDoc(uid, email = "user@moviesw.com") {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore({ databaseId: DATABASE_ID });
    await db.collection("users").doc(uid).set({
      uid,
      email,
      displayName: "Regular User",
      role: "user",
      appVersionCode: 1,
      isLocked: false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
  });
}

test("Unauthenticated user cannot read or write movies or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore({ databaseId: DATABASE_ID });
  await assertFails(unauthDb.collection("movies").get());
  await assertFails(unauthDb.collection("users").get());
});

test("Regular user can create own profile with role 'user' only", async () => {
  const userDb = testEnv.authenticatedContext(USER_UID, {
    email: "user1@example.com",
    email_verified: true,
  }).firestore({ databaseId: DATABASE_ID });

  await assertSucceeds(
    userDb.collection("users").doc(USER_UID).set({
      uid: USER_UID,
      email: "user1@example.com",
      displayName: "Viewer One",
      role: "user",
      appVersionCode: 1,
      isLocked: false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Privilege escalation attempt: user tries to create with role 'admin'
  await assertFails(
    userDb.collection("users").doc(USER_UID).set({
      uid: USER_UID,
      email: "user1@example.com",
      displayName: "Viewer One",
      role: "admin",
      appVersionCode: 1,
      isLocked: false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Regular user cannot escalate own role or read/list other users", async () => {
  await seedUserDoc(USER_UID, "user1@example.com");
  await seedUserDoc(OTHER_UID, "other@example.com");

  const userDb = testEnv.authenticatedContext(USER_UID, {
    email: "user1@example.com",
    email_verified: true,
  }).firestore({ databaseId: DATABASE_ID });

  // Cannot update role
  await assertFails(
    userDb.collection("users").doc(USER_UID).update({
      role: "admin",
      updatedAt: serverTimestamp(),
    })
  );

  // Cannot update isLocked
  await assertFails(
    userDb.collection("users").doc(USER_UID).update({
      isLocked: true,
      updatedAt: serverTimestamp(),
    })
  );

  // Can update displayName and appVersionCode
  await assertSucceeds(
    userDb.collection("users").doc(USER_UID).update({
      displayName: "Viewer Updated",
      appVersionCode: 2,
      updatedAt: serverTimestamp(),
    })
  );

  // PII isolation: cannot read other user or list users
  await assertFails(userDb.collection("users").doc(OTHER_UID).get());
  await assertFails(userDb.collection("users").get());
});

test("Spoofed unverified bootstrap admin email cannot gain admin access", async () => {
  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: "busawamwenga@gmail.com",
    email_verified: false,
  }).firestore({ databaseId: DATABASE_ID });

  await assertFails(spoofDb.collection("users").get());
  await assertFails(
    spoofDb.collection("movies").doc("movie_1").set({
      title: "Spoof Movie",
      description: "Test",
      videoUrl: "https://example.com/v.mp4",
      storagePath: "movies/movie_1.mp4",
      thumbnailUrl: "",
      uploadedBy: "spoof_uid",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );
});

test("Admin can list users, switch roles, lock accounts, and upload/delete movies", async () => {
  await seedAdminDoc(ADMIN_UID);
  await seedUserDoc(USER_UID, "viewer@example.com");

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: "admin@moviesw.com",
    email_verified: true,
  }).firestore({ databaseId: DATABASE_ID });

  const userDb = testEnv.authenticatedContext(USER_UID, {
    email: "viewer@example.com",
    email_verified: true,
  }).firestore({ databaseId: DATABASE_ID });

  // Admin can list users
  await assertSucceeds(adminDb.collection("users").get());

  // Admin can switch role to admin and lock account
  await assertSucceeds(
    adminDb.collection("users").doc(USER_UID).update({
      role: "admin",
      isLocked: true,
      updatedAt: serverTimestamp(),
    })
  );

  // Regular user (or newly created user) cannot write or delete movies
  const otherDb = testEnv.authenticatedContext(OTHER_UID, {
    email: "other@example.com",
    email_verified: true,
  }).firestore({ databaseId: DATABASE_ID });

  await assertFails(
    otherDb.collection("movies").doc("movie_1").set({
      title: "Blocked Movie",
      description: "Test",
      videoUrl: "https://example.com/v.mp4",
      storagePath: "movies/movie_1.mp4",
      thumbnailUrl: "",
      uploadedBy: OTHER_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Admin creates movie cleanly
  await assertSucceeds(
    adminDb.collection("movies").doc("movie_1").set({
      title: "Hero Film",
      description: "Great story",
      videoUrl: "https://firebasestorage.googleapis.com/movie_1.mp4",
      storagePath: "movies/movie_1.mp4",
      thumbnailUrl: "",
      fileName: "hero.mp4",
      fileSizeBytes: 123456,
      durationSeconds: 3600,
      category: "action",
      isVipOnly: false,
      uploadedBy: ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Ghost field shadow write fails
  await assertFails(
    adminDb.collection("movies").doc("movie_2").set({
      title: "Ghost Movie",
      description: "Test",
      videoUrl: "https://example.com/v.mp4",
      storagePath: "movies/movie_2.mp4",
      thumbnailUrl: "",
      ghostField: "hack",
      uploadedBy: ADMIN_UID,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    })
  );

  // Signed-in regular user can read/list movies, cannot delete
  await assertSucceeds(otherDb.collection("movies").get());
  await assertFails(otherDb.collection("movies").doc("movie_1").delete());

  // Admin can delete movie
  await assertSucceeds(adminDb.collection("movies").doc("movie_1").delete());
});
