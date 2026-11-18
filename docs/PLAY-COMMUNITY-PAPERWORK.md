# Play Console paperwork for Community (draft for Chris, Nov 18 2026)

Community adds accounts and player posts, so four Play Console pages need updating before production.
This is a draft of the answers based on what the code actually does. You make the final choices in Play Console.
Nothing here has been submitted.

## 1. Data safety form

**Does your app collect or share user data?** Yes (collect). Not shared: Cloudflare runs the server for us
(a service provider), which Play doesn't count as sharing.

**Encrypted in transit?** Yes (HTTPS only; the app refuses non-https addresses).

**Can users ask for their data to be deleted?** Yes: in the app (Community > Account > Delete my account) and on the
web at `https://apd-community.charltonchris29.workers.dev/delete`.

| Play data type | Collected? | Why | Optional? |
|---|---|---|---|
| Personal info > Name | Yes (the display name they pick) | App functionality (shown on posts) | Yes |
| Personal info > User IDs | Yes (account id and the Google account id from Google sign-in) | Account management | Yes |
| Messages > Other in-app messages | Yes (ideas and comments; there are no private messages) | App functionality | Yes |
| App activity > Other user-generated content | Yes (upvotes, tournament votes, reports, blocks) | App functionality | Yes |
| Personal info > Other info | Yes: only "passed the 16+ check" (never the birth date or age) | App functionality (age gate) | Yes |

Not collected: location, contacts, photos, files, payment info, device IDs, crash logs sent automatically (bug
reports are only shared when the player chooses to send one), advertising IDs, email addresses (Google sign-in is the only way in; the server keeps the Google account id, never the
email), passwords (there are none).

## 2. Account deletion (App content > Data deletion)

- In-app path: Community > Account > Delete my account (type DELETE to confirm).
- Web link: `https://apd-community.charltonchris29.workers.dev/delete`. The player signs in with Google there and that
  account is deleted. No email or password involved.
- What's deleted: the account, its ideas, comments, votes, reports and blocks. Comments other players replied to are
  kept as empty "[deleted]" placeholders. Nothing is kept afterwards.

## 3. Content rating questionnaire (redo it)

- Users can interact or exchange content: **Yes** (public ideas and comments; no private messages).
- Shares user location: No. Digital purchases: No (unchanged).
- Moderation: report button, block, slur filter (posts with slurs are refused, swears are masked), rate limits,
  admin hide/remove/ban.
- Community is 16+ (Play Age Signals first, then birth month/year, pass/fail only); the game stays open to everyone.

## 4. Privacy policy (https://charltonbornstudios.com/apd-privacy)

Add a Community section saying: sign-in is Google only and no email or password is stored, what's collected (table above), that Cloudflare hosts it, that it's only used to run
Community, that there's no advertising or selling, how to delete (in-app path and web link), that only a pass/fail
of the 16+ check is kept, and how to contact you.

## Also needed before Community goes live

- Done Nov 18: Cloudflare Worker + D1 deployed from `deploy/community-server`; Google OAuth web and Android clients;
  consent screen published. Launch is Google sign-in only, so no email service and no monthly cost.
