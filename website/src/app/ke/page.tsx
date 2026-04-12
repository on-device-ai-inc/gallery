import { CountryHomePage } from '@/components/country-home'

export const metadata = {
  title: 'OnDevice AI Kenya — AI Inayofanya Kazi Bila Mtandao | KSh 130',
  description:
    'AI inayofanya kazi moja kwa moja kwenye simu yako ya Android. Lipa mara moja kwa M-Pesa — KSh 130 tu. Hakuna ada ya kila mwezi.',
  alternates: {
    canonical: 'https://on-device.org/ke',
  },
}

export default function KenyaPage() {
  return (
    <CountryHomePage
      config={{
        currency: 'KES',
        countryName: 'Kenya',
        countryCode: 'ke',
        paymentMethod: 'M-Pesa',
        studentBody:
          '"Prepare for your KCPE and KCSE exams anywhere — on the matatu, between classes, even when Safaricom is completely down."',
      }}
    />
  )
}
