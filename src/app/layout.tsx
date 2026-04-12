import type { Metadata } from "next";
import { Manrope, Geist_Mono } from "next/font/google";
import "./globals.css";

const manrope = Manrope({
  variable: "--font-sans",
  subsets: ["latin"],
  display: "swap",
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "OnDevice AI — AI That Runs on Your Phone",
  description: "Powerful AI that runs entirely on your Android phone. No subscription, no network needed. Pay once, own it outright.",
  alternates: {
    canonical: 'https://on-device.org',
    languages: {
      'en-KE':     'https://on-device.org/ke',
      'en-NG':     'https://on-device.org/ng',
      'en-ZA':     'https://on-device.org/za',
      'en-GH':     'https://on-device.org/gh',
      'en-TZ':     'https://on-device.org/tz',
      'en-ZW':     'https://on-device.org/zw',
      'x-default': 'https://on-device.org',
    },
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en">
      <body className={`${manrope.variable} ${geistMono.variable} antialiased`}>
        {children}
      </body>
    </html>
  );
}
