const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validNowTimestamp() {
  return new Date(Date.now() - 1000);
}

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

test("Unauthenticated context: cannot read or write user metadata or subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("amanah_users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("amanah_users").doc(ALICE_UID).set({
      updatedAt: validNowTimestamp(),
    })
  );
  await assertFails(
    unauthDb.collection("amanah_users").doc(ALICE_UID).collection("budgets").get()
  );
});

test("Cross-user isolation: Bob cannot read, list, create, update, or delete Alice data", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("amanah_users").doc(ALICE_UID).set({
      updatedAt: validNowTimestamp(),
      totalBackedUpItems: 1,
    });
    await db
      .collection("amanah_users")
      .doc(ALICE_UID)
      .collection("wallets")
      .doc("wallet_cash")
      .set({
        id: "wallet_cash",
        name: "Dompet Tunai",
        type: "CASH",
        updatedAt: validNowTimestamp(),
      });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("amanah_users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("amanah_users").doc(ALICE_UID).collection("wallets").get()
  );
  await assertFails(
    bobDb
      .collection("amanah_users")
      .doc(ALICE_UID)
      .collection("wallets")
      .doc("wallet_cash")
      .delete()
  );
  await assertFails(
    bobDb.collection("amanah_users").doc(ALICE_UID).set({
      updatedAt: validNowTimestamp(),
      deviceName: "Spoof",
    })
  );
});

test("Unknown root collections denied by global catch-all", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("other_collection").doc("doc1").get());
  await assertFails(
    aliceDb.collection("other_collection").doc("doc1").set({ foo: "bar" })
  );
});

test("Timestamp and Ghost Field validation rejects invalid updatedAt or injected fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const aliceDoc = aliceDb.collection("amanah_users").doc(ALICE_UID);

  // Number updatedAt instead of Firestore Timestamp must fail
  await assertFails(
    aliceDoc.set({
      updatedAt: Date.now(),
    })
  );

  // Future timestamp (> request.time) must fail
  await assertFails(
    aliceDoc.set({
      updatedAt: new Date(Date.now() + 60000),
    })
  );

  // Shadow/ghost field injection must fail
  await assertFails(
    aliceDoc.set({
      updatedAt: validNowTimestamp(),
      isAdmin: true,
    })
  );

  // Oversized ID poisoning must fail
  await assertFails(
    aliceDoc.collection("budgets").doc("bad id with spaces!").set({
      id: "bad id with spaces!",
      accountId: "acc_1",
      categoryName: "Pangan",
      monthlyLimit: 1000000,
      updatedAt: validNowTimestamp(),
    })
  );
});

test("Authenticated owner: valid Kotlin-aligned writes & reads succeed across root + all 9 subcollections", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const userDoc = aliceDb.collection("amanah_users").doc(ALICE_UID);
  const ts = validNowTimestamp();

  // Root AmanahUser metadata + preferencesJson
  await assertSucceeds(
    userDoc.set({
      updatedAt: ts,
      updatedAtMillis: Date.now(),
      deviceName: "Pixel 8",
      totalBackedUpItems: 9,
      backupChecksum: "abc123def456",
      sedekahStreak: 5,
      sedekahLongest: 12,
      sedekahTotal: 250000,
      sedekahDays: 15,
      sedekahLastMillis: Date.now(),
      sedekahHistoryJson: '{"2026-11-17":10000}',
      walletMutationsJson: "[]",
      ibadahDepositsJson: "[]",
      preferencesJson: '{"appTheme":"TACTICAL"}',
    })
  );
  await assertSucceeds(userDoc.get());

  // 1. wallets
  await assertSucceeds(
    userDoc.collection("wallets").doc("wallet_1").set({
      id: "wallet_1",
      name: "Bank Syariah",
      type: "BANK_SYARIAH",
      institutionName: "BSI",
      accountNumber: "7123456",
      linkedAccountId: "acc_bank",
      colorHex: 4279282561,
      isDefault: true,
      notes: "Akun utama",
      updatedAt: ts,
    })
  );

  // 2. journal_entries
  await assertSucceeds(
    userDoc.collection("journal_entries").doc("entry_1").set({
      id: "entry_1",
      dateMillis: Date.now(),
      hijriYear: 1448,
      hijriMonth: 5,
      hijriDay: 27,
      description: "Sedekah Subuh Harian",
      transactionType: "INFAQ_SEDEKAH",
      linesJson: "[]",
      receiptJson: "",
      updatedAt: ts,
    })
  );

  // 3. ibadah_goals
  await assertSucceeds(
    userDoc.collection("ibadah_goals").doc("goal_1").set({
      id: "goal_1",
      type: "QURBAN_KAMBING",
      title: "Qurban 1448H",
      targetAmount: 3500000,
      currentAccumulated: 1500000,
      targetHijri: "1448-12",
      monthsRemaining: 7,
      linkedWalletId: "acc_bank",
      notes: "",
      isCompleted: false,
      updatedAt: ts,
    })
  );

  // 4. qardh_records
  await assertSucceeds(
    userDoc.collection("qardh_records").doc("qardh_1").set({
      id: "qardh_1",
      type: "HUTANG_SAYA",
      counterpartyName: "Ahmad",
      totalAmount: 1000000,
      remainingAmount: 500000,
      agreementDateMillis: Date.now() - 86400000,
      dueDateMillis: Date.now() + 86400000 * 3,
      witnessName: "Umar",
      notes: "Qardh Hasan",
      status: "AKTIF",
      installmentsJson: "[]",
      updatedAt: ts,
    })
  );

  // 5. budgets
  await assertSucceeds(
    userDoc.collection("budgets").doc("budget_1").set({
      id: "budget_1",
      accountId: "acc_food",
      categoryName: "Konsumsi Halal",
      monthlyLimit: 2000000,
      iconKey: "food",
      alertThresholdPercent: 0.8,
      updatedAt: ts,
    })
  );

  // 6. infaq_rules
  await assertSucceeds(
    userDoc.collection("infaq_rules").doc("rule_1").set({
      id: "rule_1",
      title: "Infaq Jumat 5%",
      targetCategory: "EXPENSE",
      calculationType: "PERCENTAGE",
      rate: 0.05,
      fixedAmount: 0,
      roundUpStep: 5000,
      enableFridayMultiplier: true,
      enableRamadanMultiplier: true,
      updatedAt: ts,
    })
  );

  // 7. recurring_transactions
  await assertSucceeds(
    userDoc.collection("recurring_transactions").doc("rec_1").set({
      id: "rec_1",
      title: "Internet Masjid",
      type: "EXPENSE",
      amount: 350000,
      categoryAccountId: "acc_living",
      assetAccountId: "acc_bank",
      frequency: "MONTHLY",
      dayOfMonthOrWeek: 15,
      customInfaqRate: 0.025,
      enableRoundUp: false,
      roundUpStep: 5000,
      isActive: true,
      autoExecute: false,
      lastExecutedDateMillis: -1,
      lastExecutedPeriodKey: "",
      lastExecutionTimestamp: 0,
      nextDueDateMillis: Date.now() + 86400000,
      note: "Tagihan rutin",
      updatedAt: ts,
    })
  );

  // 8. infaq_distributions
  await assertSucceeds(
    userDoc.collection("infaq_distributions").doc("dist_1").set({
      id: "dist_1",
      amount: 250000,
      recipientName: "Panti Yatim Amanah",
      asnafCategory: "YATIM_DHUAFA",
      distributionDateMillis: Date.now(),
      hijriDateString: "27 Jumadil Awal 1448",
      sourceAccountId: "acc_bank",
      programName: "Santunan Bulanan",
      receiptNumber: "INV-001",
      notes: "Tersalur",
      isVerified: true,
      updatedAt: ts,
    })
  );

  // 9. rezeki_non_tunai
  await assertSucceeds(
    userDoc.collection("rezeki_non_tunai").doc("rezeki_1").set({
      id: "rezeki_1",
      title: "Traktiran Makan Siang",
      giverName: "Anonim",
      category: "FOOD_BEVERAGE",
      estimatedValue: 100000,
      suggestedInfaqRate: 0.05,
      suggestedInfaqAmount: 5000,
      isPaidInfaq: true,
      paidJournalEntryId: "entry_1",
      dateMillis: Date.now(),
      hijriDateString: "27 Jumadil Awal 1448",
      notes: "Alhamdulillah",
      createdAtMillis: Date.now(),
      updatedAt: ts,
    })
  );

  // Verify owner list queries succeed across subcollections
  await assertSucceeds(userDoc.collection("wallets").get());
  await assertSucceeds(userDoc.collection("budgets").get());
  await assertSucceeds(userDoc.collection("infaq_rules").get());
  await assertSucceeds(userDoc.collection("recurring_transactions").get());
  await assertSucceeds(userDoc.collection("infaq_distributions").get());
  await assertSucceeds(userDoc.collection("rezeki_non_tunai").get());
});
